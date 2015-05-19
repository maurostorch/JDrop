package com.almasb.jdrop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

import com.dropbox.core.*;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxEntry.Folder;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;

public class JDropMain {

    private static DbxClient client;

    private static String getAccessToken() {
        String token = "";
        Path tokenFile = Paths.get("access.token");

        if (Files.exists(tokenFile)) {
            try (BufferedReader br = Files.newBufferedReader(tokenFile)) {
                token = br.readLine();
            }
            catch (Exception e) {
                System.out.println("Error reading access.token: " + e.getMessage());
            }
        }
        else {
            System.out.println("access.token file not found");
        }

        return token;
    }

    private static void processCommand(String command, String... params) throws Exception {
        switch (command) {
            case "ls":
                String path = "/" + (params.length > 0 ?  params[0] : "");
                DbxEntry.WithChildren listing = client.getMetadataWithChildren(path);
                for (DbxEntry child : listing.children) {
                    System.out.println(child.name + (child.isFolder() ? "/" : ""));
                }
                break;

            case "get":
                if (params.length != 2)
                    throw new IllegalArgumentException("Must pass filename and localfolder as parameter");

                String localFolder = params[1].endsWith("/")?params[1]:params[1]+"/";
		Files.createDirectories(Paths.get(localFolder).getParent());
		String filename = params[0].substring(params[0].lastIndexOf("/")+1);
                FileOutputStream outputStream = new FileOutputStream(localFolder + filename);
                try {
                    DbxEntry.File downloadedFile = client.getFile(params[0], null, outputStream);
                    System.out.println("Downloaded: " + downloadedFile.name);
                }
                finally {
                    outputStream.close();
                }
                break;

            case "put":
                if (params.length != 2)
                    throw new IllegalArgumentException("Must pass filename and destination as parameter");

                File file = new File(params[0]);
                DbxEntry.File uploadedFile = client.uploadFile(params[1] + file.getName(),
                        DbxWriteMode.add(),
                        file.length(),
                        new FileInputStream(file));
                System.out.println("Uploaded: " + uploadedFile.name);
                break;

            case "mkdir":
                if (params.length == 0)
                    throw new IllegalArgumentException("Must pass dir name as parameter");

                Folder folder = client.createFolder("/" + params[0]);
                System.out.println("Created dir: " + folder.name);
                break;

            case "help":    // fallthru
            default:
                printHelp();
                break;
        }
    }

    private static void printHelp() {
        System.out.println("help\t print this message");
        System.out.println("ls\t print files and folders in root directory");
        System.out.println("ls foldername\t print files and folders in foldername directory");
        System.out.println("get filename localfolder\t downloads filename file from Dropbox account to local machine");
        System.out.println("put filename remotefolder\t uploads filename file from local machine to Dropbox account");
        System.out.println("mkdir foldername\t creates foldername directory");
        System.out.println("exit\t exit program");
    }

    public static void main(String[] args) throws Exception{
        System.out.println("Connecting...");

        String token = getAccessToken();
        DbxRequestConfig config = new DbxRequestConfig("JDrop/0.1", Locale.getDefault().toString());
	Scanner input = new Scanner(System.in);
        if (token.isEmpty()) {
            //System.out.println("Cannot proceed without access token. Exiting");
            //return;
	    DbxAppInfo appinfo = new DbxAppInfo("tsfw17duz6moze6","8ibzyo5ms367trh");
	    DbxWebAuthNoRedirect r = new DbxWebAuthNoRedirect(config,appinfo);
	    System.out.println(r.start());
	    System.out.print("Type your code here:");
	    DbxAuthFinish f = r.finish(input.nextLine());
	    token = f.accessToken;
	    FileOutputStream out = new FileOutputStream("access.token");
	    out.write(token.getBytes("UTF-8"));
	    out.close();

        }

        client = new DbxClient(config, token);
	

        try {
            System.out.println("Connected to Dropbox account: " + client.getAccountInfo().displayName);
        }
        catch (DbxException e) {
            System.out.println("Error retrieving Dropbox account info: " + e.getMessage());
        }


        String command = "help";
        while (!"exit".equals(command)) {
            String[] tokens = command.split(" +");

            try {
                if (tokens.length == 1) {
                    processCommand(tokens[0]);
                }
                else if (tokens.length > 1) {
                    processCommand(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
                }
            }
            catch (Exception e) {
                System.out.println("Error occurred during command processing: " + e.getMessage());
            }

            System.out.print("> ");
            command = input.nextLine();
        }

        input.close();
    }
}
