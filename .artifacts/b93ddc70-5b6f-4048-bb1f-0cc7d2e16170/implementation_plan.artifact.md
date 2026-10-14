# Create Preview for LoginScreen

The goal is to add a `@Preview` for the `LoginScreen` composable in `LoginScreen.kt`. As per the requirements, I will extract a stateless version of the composable that takes state variables as parameters and use it for the preview.

## Proposed Changes

### UI Screens

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/sujai/AndroidStudioProjects/SNDPMSNattika/app/src/main/java/com/example/sndpmsnattika/ui/screens/LoginScreen.kt)

- Extract `LoginScreenContent` from `LoginScreen`.
- Pass state and callbacks from `LoginScreen` to `LoginScreenContent`.
- Add `LoginScreenPreview` using `LoginScreenContent` and `SNDPMSNattikaTheme`.

## Verification Plan

### Automated Verification
- Run `analyze_file` on `LoginScreen.kt` to check for syntax errors.
- Run `render_compose_preview` to verify the new preview.
