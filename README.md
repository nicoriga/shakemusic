shakemusic
==========

AccelerAudio
Last modified: April 9, 2014.


Design and implement an app that records data coming from the accelerometer in your device, and uses them to create music.
Accelerometer data, together with audio parameters that define how to create music from them (see below), constitute a music session. The app should manage and store multiple music sessions. Two or more music sessions can share the same acceleration data.
The app should provide at least the following user interfaces (UIs) and functions.

1. A UI that lists available music sessions.
At least the following information must be displayed for each music session:
music session name,
date when audio parameters were last modified for the music session,
a thumbnail picture that is unique for each music session (a �signature� of the session, so to speak) and is generated from recorded acceleration data. How the signature is generated is entirely left to the group.
The UI should also provide a way (button, menu, long-press contextual menu) to perform the following functions.
Start a new music session by displaying UI#3 (see below) to record new accelerometer data.
Duplicate an existing music session so that a different music can be generated from existing accelerometer data. In the duplicate session, the date when parameters were last modified is to be set to the current date.
Delete an existing music session.
Rename a music session. The name of the session is not considered an audio parameter, so the date when parameters were last modified must not be updated when a session is renamed.
Play a music session. If this function is activated, UI#4 is displayed (see below).

2. A separate UI that shows the details of a music session and allows to specify how accelerometer data should be processed to create music.
As far as details are concerned, the following is a bare-minimum list of information that should be displayed:
music session name,
thumbnail �signature� picture for the music session,
date when accelerometer data were recorded,
date when audio parameters were last modified for the music session.
Concerning data processing, it is entirely left to the group to decide how accelerometer data are manipulated and mixed to create music. However, the group must remember that 3 sets of data are available from the acceleration sensor (one along each axis) and must allow the user to include any subset of them in the music creation process. Furthermore, the group should be aware that acceleration data should be upsampled in some way to obtain music of meaningful duration (even at the lowest audio quality, thousands of acceleration samples are required for each second of music). Even the simplest form of upsampling, i.e., repeating each sample several times in a row, is acceptable. All in all, the UI should provide at least the following facilities to govern data processing.
A selector (e.g, a set of three checkboxes) that allows the user to decide which accelerometer axes contribute to the music creation process.
A selector that allows the user to decide the amount of upsampling.
A �Play� button. When the button is pressed, UI#4 is displayed (see below).
The UI should also provide a way to rename a music session.
When the user leaves the UI and audio parameters (e.g., the status of the set of checkboxes) have actually been changed, the modification date should be updated accordingly by setting it to the current date.
Sophisticated processing functions (e.g., filters, influencing music via the touch screen) may give the group bonus points: make arrangements with the instructor.

3. A separate UI for recording new accelerometer data.
The UI should provide the following functions:
give a name to the recording session,
start the recording process,
pause the recording process,
resume the recording process,
end the recording process and leave this UI for UI#2, where audio parameters can be set. Once the user leaves this UI, the recording cannot be modified (i.e., no new accelerometer data can be added) by coming back to it.
The UI should also display at least the following summary information about the recording session:
number of samples recorded so far,
a visual clue (bars, charts�) about the signal level coming from each of the 3 axes of the accelerometer.
The recording process must continue when the app is moved to the background.

4. A UI that is displayed while music playback is in progress.
Music starts to play indefinitely in a continuous loop when this UI is first displayed. Music must continue to play when the app is moved to the background.
The UI should provide the following functions:
pause music playback,
resume music playback,
end music playback, in which case the user is brought back to UI#1 or UI#2, as appropriate.
The UI should also display the name of the music session, the duration of the music session and the playback position inside the session. The possibility of skipping audio back and forth is optional.

5. A �Preferences� UI, implemented according to the interface guidelines of the chosen platform. The following preferences are mandatory:
default accelerometer axes to include in the music creation process (see UI#2),
sample rate used to record accelerometer data (see UI#3),
maximum duration of a recording session (see UI#3),
default amount of upsampling (see UI#2).

All UIs must be correctly displayed and profitably usable in both portrait and landscape mode. When appropriate, the layout of a UI must be different in portrait and landscape mode.

UI#1 can be merged with UI#2 if the display is big enough (e.g., the application is run on a tablet computer).
UI#4 can be embedded into UIs #1 and #2 if the group prefers to do so.

Error messages should be presented to the user when appropriate (e.g., when no more space is available for storing data, when no audio device is available for playback, etc.).

Accelerometer data and audio parameters can be stored in any format the group sees fit.

The introduction of a splash screen is discouraged; its presence will not give the group any bonus points.

The state of the app must be fully preserved when the app itself loses the foreground. For instance, if the app is moved to the background while the user is setting audio parameters (UI#2), UI#2 must be shown again as soon as the app regains the foreground, and parameters should not be lost as well.
Recording of accelerometer data and audio playback must continue even when the app loses the foreground.

The names of buttons provided above are only for description purposes and they are not binding: a group can decide to show a different text, or display information in a graphical fashion instead.

It is forbidden to use third-party libraries for UI development or audio processing without explicit consent from the instructor.
