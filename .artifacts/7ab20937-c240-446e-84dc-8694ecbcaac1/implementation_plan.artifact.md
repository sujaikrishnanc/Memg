# Implementation Plan - Admin Management: Add Member

This plan outlines the steps to implement the Admin functionality for adding new members with a default password and mandatory first-login password change.

## User Review Required

> [!NOTE]
> The member data will be persisted using Android DataStore for simplicity in this mock implementation.

## Proposed Changes

### Data Layer

#### [MODIFY] [AuthRepository.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/data/AuthRepository.kt)
- Update `AuthRepository` to store a list of members in DataStore.
- Implement `addMember(mobile: String)` which adds the member to the store.
- Update `login(mobile: String, password: String)` to:
    - Validate if the member exists.
    - Check if the password is the default 'sndpmsnattika2026'.
    - Determine if `forcePasswordChange` is required (based on whether they've changed it before).
- Implement `setPasswordChangedForUser(mobile: String, changed: Boolean)`.

### Navigation

#### [MODIFY] [NavKeys.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/navigation/NavKeys.kt)
- Add `AddMember` to `NavRoute` sealed interface.

### UI Layer

#### [NEW] [AddMemberScreen.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/screens/AddMemberScreen.kt)
- Create a screen for Admin to enter a member's mobile number.
- Include validation for mobile number (e.g., length and digits).
- Show success/error messages.

#### [NEW] [AddMemberViewModel.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/viewmodels/AddMemberViewModel.kt)
- Handle the logic for adding a member via `AuthRepository`.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/screens/DashboardScreen.kt)
- Update the FAB in `DashboardList` to take an `onAddMember` callback.
- Pass this callback from `DashboardScreen` to `DashboardList`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/MainActivity.kt)
- Add `NavRoute.AddMember` to the `NavDisplay`.
- Handle navigation to `AddMember` from `DashboardScreen`.

## Verification Plan

### Automated Tests
- Unit tests for `AuthRepository` to verify `addMember` and `login` logic.
- Run `./gradlew :app:testDebugUnitTest`

### Manual Verification
1. Log in as Admin (mobile starting with 9).
2. Click the FAB "Add Member".
3. Enter a member's mobile number and save.
4. Log out.
5. Log in as the new member using the default password 'sndpmsnattika2026'.
6. Verify that the app redirects to the "Change Password" screen.
7. Change the password and verify redirection to the Dashboard.
8. Log out and log in again with the new password; verify no redirect to "Change Password" occurs.
