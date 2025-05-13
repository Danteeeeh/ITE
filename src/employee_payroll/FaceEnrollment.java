package employee_payroll;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FaceEnrollment extends javax.swing.JFrame {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private VideoCapture capture;
    private Mat frame;
    private CascadeClassifier faceDetector;
    private Timer timer;
    private JLabel cameraFeed;
    private JButton captureButton;
    private JButton saveButton;
    private JTextField empIdField;
    private List<Mat> capturedFaces;
    private boolean isCapturing = false;

    public FaceEnrollment() {
        initComponents();
        initializeOpenCV();
        capturedFaces = new ArrayList<>();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Face Enrollment");

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Camera feed panel
        cameraFeed = new JLabel();
        cameraFeed.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cameraFeed.setPreferredSize(new Dimension(640, 480));
        mainPanel.add(cameraFeed, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        JLabel empIdLabel = new JLabel("Employee ID:");
        empIdField = new JTextField(10);
        captureButton = new JButton("Capture Face");
        saveButton = new JButton("Save & Train");
        saveButton.setEnabled(false);

        controlPanel.add(empIdLabel);
        controlPanel.add(empIdField);
        controlPanel.add(captureButton);
        controlPanel.add(saveButton);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        captureButton.addActionListener(e -> startCapture());
        saveButton.addActionListener(e -> saveFaceData());

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeOpenCV() {
        frame = new Mat();
        capture = new VideoCapture(0);
        faceDetector = new CascadeClassifier();
        faceDetector.load("haarcascade_frontalface_alt.xml");
    }

    private void startCapture() {
        if (!capture.isOpened()) {
            capture.open(0);
        }

        String empId = empIdField.getText().trim();
        if (empId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Employee ID");
            return;
        }

        isCapturing = true;
        captureButton.setEnabled(false);
        capturedFaces.clear();

        timer = new Timer(33, e -> {
            if (isCapturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    MatOfRect faceDetections = new MatOfRect();
                    faceDetector.detectMultiScale(grayFrame, faceDetections);

                    for (Rect rect : faceDetections.toArray()) {
                        Imgproc.rectangle(frame, rect, new Scalar(0, 255, 0), 2);
                        if (capturedFaces.size() < 5) { // Capture 5 face samples
                            Mat face = new Mat(grayFrame, rect);
                            Mat resizedFace = new Mat();
                            Size size = new Size(200, 200);
                            Imgproc.resize(face, resizedFace, size);
                            capturedFaces.add(resizedFace);
                        } else {
                            isCapturing = false;
                            timer.stop();
                            saveButton.setEnabled(true);
                            JOptionPane.showMessageDialog(this, "Face samples captured successfully!");
                        }
                    }

                    BufferedImage image = matToBufferedImage(frame);
                    cameraFeed.setIcon(new ImageIcon(image));
                }
            }
        });
        timer.start();
    }

    private void saveFaceData() {
        try {
            int empId = Integer.parseInt(empIdField.getText().trim());
            
            // Save face data to database
            Connection conn = db.java_db();
            for (Mat face : capturedFaces) {
                MatOfByte mob = new MatOfByte();
                Imgcodecs.imencode(".jpg", face, mob);
                byte[] faceBytes = mob.toArray();

                String query = "INSERT INTO employee_face_data (emp_id, face_encoding) VALUES (?, ?)";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setInt(1, empId);
                pst.setBytes(2, faceBytes);
                pst.executeUpdate();
            }

            // Retrain face recognition model
            trainModel();

            JOptionPane.showMessageDialog(this, "Face data saved and model trained successfully!");
            this.dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving face data: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Employee ID");
        }
    }

    private void trainModel() {
        try {
            Connection conn = db.java_db();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT emp_id, face_encoding FROM employee_face_data");

            List<Mat> faces = new ArrayList<>();
            List<Integer> labels = new ArrayList<>();

            while (rs.next()) {
                byte[] faceBytes = rs.getBytes("face_encoding");
                Mat face = Imgcodecs.imdecode(new MatOfByte(faceBytes), Imgcodecs.IMREAD_GRAYSCALE);
                faces.add(face);
                labels.add(rs.getInt("emp_id"));
            }

            if (!faces.isEmpty()) {
                FaceRecognizer recognizer = LBPHFaceRecognizer.create();
                MatOfInt labelsMat = new MatOfInt();
                labelsMat.fromList(labels);
                recognizer.train(faces, labelsMat);
                recognizer.save("face_model.yml");
            }

            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error training model: " + e.getMessage());
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FaceEnrollment().setVisible(true);
        });
    }
}
