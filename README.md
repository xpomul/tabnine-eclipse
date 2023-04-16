# tabnine-eclipse
Eclipse Plugin to use AI-based code completion provided by TabNine (see https://tabnine.com for details).

Be aware that this plugin will doanload and run a binary by TabNine on your PC which functions as a client for their cloud server.

I am not in any way affiliated with TabNine - Use this software at your own risk!

## How to use

There is currently no update site; clone the code for yourself, `mvn clean package` it and use your locally built update site.

After installing in your Eclipse IDE (I have used 2022_06 as target platform; any Eclipse release earlier or later may or may not work),
go to the preferences to change the Tabnine settings to your liking.

You can either switch TabNine completely off, then it will never activate itself in any case.

The default is that TabNine is in on-demand mode which means that it is not active by default for any newly opened text/source editor.

* To activate it, use M1+RETURN (M1 is CMD on Mac, and CTRL on Linux/Windows). It will take a few seconds until the binary is started.
* To toggle the activation state, use M1+Numpad 0.
* To accept the default proposal (0:), use M1+RETURN
* To accept any other proposal (1 to 6), use M1 + the corresponding key on the NUMPAD.

To access the TabNine configuration, just write `Tabnine::config` into any text editor in which TabNine is active.

See https://support.tabnine.com/hc/en-us/articles/4413854085265-What-are-the-special-commands-that-can-be-given-to-Tabnine- for hints about this.
