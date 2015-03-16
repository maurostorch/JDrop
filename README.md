# JDrop
CLI access to Dropbox using common FTP commands

# Instructions
1. Create an app in App Console: https://www.dropbox.com/developers/apps
2. Register it with the Dropbox account you want to FTP to
3. Generate access token and put it in a file "access.token" in directory with JDrop
Note: DO NOT share your access token - anyone who has it can gain access to the Dropbox account
4. Run JDrop

If steps were successfully completed, JDrop will display:
```bash
Connecting...
Connected to X Y
```
where X Y is the full name of Dropbox account

# Using JDrop
JDrop supports common FTP commands: ls, get, put, etc
You can type help to gain more information about available commands in your build
Type exit to close JDrop

