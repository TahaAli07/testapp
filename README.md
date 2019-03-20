# testapp
A simple management app

What are you building: A simple visitor management app. See here to get a better idea - https://www.youtube.com/watch?v=tMGUzwgZC58 . The main challenge of this app will be to see if you can understand API and able to use them. Also if you can use material design.

Api : You will use Firebase api to do two things: store visitor data and send OTP verification

Firebase getting started - https://firebase.google.com/docs/android/setup
Firebase read and write api - https://firebase.google.com/docs/database/android/start
Firebase SMS OTP api - https://firebase.google.com/docs/auth/android/phone-auth
Firebase storage api (for photos) - https://firebase.google.com/docs/storage/android/upload-files

Photo taking in android - wherever below you have to take photos, you will use Camerakit to take photos. Do NOT use default android api. https://github.com/CameraKit/camerakit-android

Background processing - you will Rxjava to do tasks in background (like rxjava)

What features will you build:

1. Visitor will enter his own phone number and photo (using camera) on the screen. Here you will do checks for valid phone number (10 digits, etc). You will also compress the photo IN THE BACKGROUND using Rxjava (https://github.com/zetbaitsu/Compressor)
2. you will first check if phone already exists in the firebase database. You will use addListenerForSingleValueEvent    to do this (https://stackoverflow.com/a/39800981/112050)
3. If number does NOT exist (meaning new visitor)
  3.1 you will send OTP to the phone number (using Firebase SMS OTP api) and show screen to enter OTP
  3.2 If incorrect OTP is entered OR 30 seconds time has passed
     3.2.1 show message that OTP is incorrect
     3.2.2 You will then upload the compressed photo to Firebase storage, and get the photo download url (using api).
     3.2.3 create a unique key for visitor - https://stackoverflow.com/questions/48705124/how-can-i-create-a-unique-key-and-use-it-to-send-data-in-firebase
     3.2.4 you will then save the photo download url and phone number under the unique key in firebase - all under "suspicious_users"
     3.2.5 go to step 1

  3.3 If correct OTP is entered,
     3.3.1 You will then upload the compressed photo to Firebase storage, and get the photo download url (using api).
     3.3.2 you will then create a unique key for visitor - https://stackoverflow.com/questions/48705124/how-can-i-create-a-unique-key-and-use-it-to-send-data-in-firebase
     3.3.3 you will also create an integer "visit_count" and set it to value 1
     3.3.4 you will then save the photo download url, phone number and "visit_count" under the unique key in firebase - all under "visitors"
     3.3.4. Show message "New Visitor Saved!"

4. If number already exists (meaning repeat visitor)
  4.1. get unique id of this visitor from the "visitors" section
  4.2  get "visit_count" of this visitor
  4.3 increment "visit_count" by 1
  4.4. update visit_count in firebase database (using update api)
  4.5. show a message - "welcome back for <visit_count> time" . Replace <visit_count> with the actual value of visit_count

3. If there is any error, then there will be error shown using "Material Design Snackbar" (make sure you use Google Material Design Support Library for this - https://developer.android.com/topic/libraries/support-library/features#material-design)

Evaluation - we will evaluate how you have built the user interface. We will also check the style of the code.

Extra  points - if you can do the above in Kotlin