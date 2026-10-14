# Update Firebase Authentication to use Mobile Numbers

This plan outlines the changes required to use mobile numbers (with country codes) as the primary identifier for user authentication, mapping them to synthetic emails for Firebase Auth.

## Proposed Changes

### Data Layer

#### [MODIFY] [AuthRepository.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/data/AuthRepository.kt)
- Update synthetic email domain from `@sndpmsnattika.com` to `@sndpmsnattika.org`.
- Refactor `login` to handle mobile numbers with country codes.
- Implement `resetPassword` to update the password in Firebase Auth and Firestore after OTP verification.
- Ensure `addMember` correctly stores the mobile number with country code.

### View Models

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/viewmodels/LoginViewModel.kt)
- Add validation for mobile number (must start with `+`).

#### [MODIFY] [ForgotPasswordViewModel.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/viewmodels/ForgotPasswordViewModel.kt)
- Add validation for mobile number (must start with `+`).
- Pass the full mobile number to the repository.

### UI Layer

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/screens/LoginScreen.kt)
- Update `OutlinedTextField` for mobile number to include a visual prefix or hint for the country code.
- Add basic validation display.

#### [MODIFY] [ForgotPasswordRequestScreen.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/screens/ForgotPasswordRequestScreen.kt)
- Update `OutlinedTextField` for mobile number to include a visual prefix or hint for the country code.

## Verification Plan

### Automated Tests
- Run `AuthRepositoryTest.kt` after updates to ensure synthetic email mapping works as expected.

### Manual Verification
- Test login with a mobile number like `+919876543210`.
- Verify first-time login behavior with default password `sndpmsnattika2026`.
- Verify the Forgot Password flow:
    1. Enter mobile number with country code.
    2. Enter mock OTP `123456`.
    3. Reset password and verify successful login with the new password.
