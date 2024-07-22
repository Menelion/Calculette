# Calculette

A test Android non-visual calculator app.

## Implemented Features

* The expression is input in the only edit box. The keyboard is not restricted since the underlying library supports many advanced operators like `^` for power, parentheses, fractions and many more.
* The result is calculated on pressing Enter and is displayed in the same edit field. The cursor is set to the end of the field in this case.
* If *Ctrl+M* is pressed, a pseudo-menu opens with four basic arithmetic operators. You can select one of them with up and down arrows, then press *Enter*. If *Escape* is pressed, the menu is dismissed.
* Everything in the calculator is self-voicing. It supports left and right arrows in the edit box, erasing with BackSpace as well as the pseudo-menu.
* The app is properly localizable. English, Ukrainian, and russian localizations are provided.

## Building

The app was written using Java 17 by Microsoft and command-line tools downloaded from Android Studio download page. So basically the following is enough from the main directory:

```shell
./gradlew clean assembleDebug
```

# Known Issues

* The app was tested on Android 9 with several speech engines. Sometimes self-voicing is somewhat unstable. If testing with TalkBack, you'll hear characters announced twice which is normal.
* The "context menu" string, although is present, is most often not announced by the self-voicing.
