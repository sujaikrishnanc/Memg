# Implementation Plan - Add @Preview for MembersScreen

The goal is to add a Compose Preview for the `MembersScreen` Composable. Since `MembersScreen` depends on a `ViewModel`, I will extract the UI logic into a stateless Composable `MembersScreenContent` and then provide a preview for it using sample data.

## Proposed Changes

### [app]

#### [MODIFY] [MembersScreen.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/screens/MembersScreen.kt)

1.  Extract the stateless UI logic from `MembersScreen` into `MembersScreenContent`.
2.  Update `MembersScreen` to call `MembersScreenContent`.
3.  Add `MembersScreenPreview` at the bottom of the file.
4.  Add necessary imports for Preview and the project theme.

## Verification Plan

### Manual Verification
1.  Render the `MembersScreenPreview` using the `render_compose_preview` tool to ensure it looks correct and contains the sample data.
