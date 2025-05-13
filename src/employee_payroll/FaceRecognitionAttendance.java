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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionAttendance extends javax.swing.JFrame {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private VideoCapture capture;
    private Mat frame;
    private CascadeClassifier faceDetector;
    private FaceRecognizer recognizer;
    private Timer timer;
    private JLabel cameraFeed;
    private JButton startButton;
    private JButton stopButton;
    private boolean isCapturing = false;

    public FaceRecognitionAttendance() {
        initComponents();
        initializeOpenCV();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Facial Recognition Attendance");
        
        cameraFeed = new JLabel();
        cameraFeed.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cameraFeed.setPreferredSize(new Dimension(640, 480));

        startButton = new JButton("Start Recognition");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startRecognition());
        stopButton.addActionListener(e -> stopRecognition());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        setLayout(new BorderLayout());
        add(cameraFeed, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void initializeOpenCV() {
        frame = new Mat();
        capture = new VideoCapture(0);
        faceDetector = new CascadeClassifier();
        faceDetector.load("haarcascade_frontalface_alt.xml");
        recognizer = LBPHFaceRecognizer.create();
        loadTrainedModel();
    }

    private void loadTrainedModel() {
        try {
            recognizer.read("face_model.yml");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading face recognition model: " + e.getMessage());
        }
    }

    private void startRecognition() {
        if (!capture.isOpened()) {
            capture.open(0);
        }

        isCapturing = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        timer = new Timer(33, e -> {
            if (isCapturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    MatOfRect faceDetections = new MatOfRect();
                    faceDetector.detectMultiScale(grayFrame, faceDetections);

                    for (Rect rect : faceDetections.toArray()) {
                        Mat face = new Mat(grayFrame, rect);
                        int[] label = new int[1];
                        double[] confidence = new double[1];
                        recognizer.predict(face, label, confidence);

                        if (confidence[0] < 100) { // Threshold for recognition confidence
                            markAttendance(label[0]);
                            Imgproc.rectangle(frame, rect, new Scalar(0, 255, 0), 2);
                        } else {
                            Imgproc.rectangle(frame, rect, new Scalar(0, 0, 255), 2);
                        }
                    }

                    BufferedImage image = matToBufferedImage(frame);
                    cameraFeed.setIcon(new ImageIcon(image));
                }
            }
        });
        timer.start();
    }

    private void stopRecognition() {
        isCapturing = false;
        if (timer != null) {
            timer.stop();
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void markAttendance(int empId) {
        try {
            Connection conn = db.java_db();
            LocalDateTime now = LocalDateTime.now();
            
            String checkQuery = "SELECT * FROM attendance WHERE emp_id = ? AND date = CURRENT_DATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, empId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                String insertQuery = "INSERT INTO attendance (emp_id, date, time_in, status, verification_method) " +
                                   "VALUES (?, CURRENT_DATE, CURRENT_TIME, ?, 'Face')";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, empId);
                
                // Determine if late based on attendance settings
                String settingsQuery = "SELECT work_start_time FROM attendance_settings LIMIT 1";
                Statement settingsStmt = conn.createStatement();
                ResultSet settingsRs = settingsStmt.executeQuery(settingsQuery);
                
                if (settingsRs.next()) {
                    Time workStartTime = settingsRs.getTime("work_start_time");
                    String status = now.toLocalTime().isAfter(workStartTime.toLocalTime()) ? "Late" : "Present";
                    insertStmt.setString(2, status);
                } else {
                    insertStmt.setString(2, "Present");
                }
                
                insertStmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Attendance marked successfully!");
            }
            
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error marking attendance: " + e.getMessage());
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
            new FaceRecognitionAttendance().setVisible(true);
        });
    }
}
