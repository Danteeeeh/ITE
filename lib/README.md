# OpenCV Face Recognition Dependencies

To enable face recognition functionality, you need to add the following files to this directory:

1. Download OpenCV 4.5.4 or later from: https://opencv.org/releases/
2. Copy the following files to this directory:
   - opencv-454.jar
   - opencv_java454.dll (Windows) or libopencv_java454.so (Linux)
   - opencv_face454.dll (Windows) or libopencv_face454.so (Linux)

3. Download the face detection cascade file:
   - Download `haarcascade_frontalface_alt.xml` from OpenCV's GitHub repository
   - Place it in this directory

## Required Files:
- opencv-454.jar
- opencv_java454.dll
- opencv_face454.dll
- haarcascade_frontalface_alt.xml

## Installation Steps:
1. Create a new directory named `lib` if it doesn't exist
2. Copy all required files to the `lib` directory
3. Add opencv-454.jar to your project's classpath
4. Ensure the DLL/SO files are in your system's PATH or in the lib directory

Note: The version numbers (454) may be different depending on your OpenCV version. Adjust accordingly.
