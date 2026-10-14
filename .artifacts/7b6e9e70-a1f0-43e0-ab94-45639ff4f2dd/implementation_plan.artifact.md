# Implementation Plan - Add Member Validations and Review Flow

This plan outlines the enhancements to the "Add Member" process, including stricter validations and a confirmation step for administrators to verify data before final submission.

## Proposed Changes

### [Data & Logic]

#### [MODIFY] [AddMemberViewModel.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/viewmodels/AddMemberViewModel.kt)
- Update `AddMemberUiState` to include a `Reviewing` state.
- Add validation for phone numbers (10 digits or E.164 format).
- Add validation to ensure at least one family member is present.
- Implement a `reviewMember()` method that performs validations and transitions to `Reviewing`.
- Update `addMember()` to be the final confirmation step.

### [UI Components]

#### [MODIFY] [AddMemberScreen.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/screens/AddMemberScreen.kt)
- Implement a confirmation dialog or a bottom sheet that shows all entered data when the UI state is `Reviewing`.
- Update the "Add Member" button to first trigger `reviewMember()`.
- The confirmation view will have "Confirm" (calls `addMember`) and "Edit" (returns to `Idle`).

## Verification Plan

### Manual Verification
- Attempt to add a member with an invalid phone number format and verify the error message.
- Attempt to add a member with no family members and verify the error message.
- Enter valid data, click "Add Member", verify that a summary appears.
- In the summary, verify all details are correct.
- Click "Edit" in the summary, change a value, and re-review.
- Click "Confirm" in the summary and verify the member is added successfully.
