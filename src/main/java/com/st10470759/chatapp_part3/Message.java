/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.st10470759.chatapp_part3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Message class for QuickChat (Part 3).
 * The structure (the message loop, generateMessageID, createMessageHash and the report
 * methods) is based on the Message class example shared by the lecturer in class,
 * changed to match the Part 2 and Part 3 briefs.
 *
 * @author rnndo
 */
public class Message {

    // The arrays asked for in the brief
    ArrayList<String> sentMessages = new ArrayList<>();
    ArrayList<String> disregardedMessages = new ArrayList<>();
    ArrayList<String> storedMessages = new ArrayList<>();
    ArrayList<String> messageHashes = new ArrayList<>();
    ArrayList<String> messageIDs = new ArrayList<>();

    // These arrays hold the other details of every message.
    // The same position in all of the "every message" arrays belongs to the same message.
    ArrayList<String> messageRecipients = new ArrayList<>();
    ArrayList<String> messageSenders = new ArrayList<>();
    ArrayList<String> messageTexts = new ArrayList<>();
    ArrayList<String> messageFlags = new ArrayList<>();   // "Sent", "Stored" or "Disregarded"

    Random random = new Random();

    // We reuse the Login class to check the recipient number
    Login login = new Login();

    String sender = "Unknown";                    // the name of the logged-in user
    String storageFile = "storedMessages.json";   // the file the stored messages are saved in

    int messagesAllowed = 0;   // how many messages the user said they want to enter
    int messagesEntered = 0;   // how many messages the user has dealt with so far

    // Saves how many messages the user wants to enter
    public void setMessagesAllowed(int number) {
        messagesAllowed = number;
    }

    // Saves the name of the user who is sending the messages
    public void setSender(String senderName) {
        sender = senderName;
    }

    // Lets us choose which file the stored messages are saved in (the tests use their own file)
    public void setStorageFile(String fileName) {
        storageFile = fileName;
    }

    // Asks the user for each message and lets them send, disregard or store it
    public void sendingMessages(Scanner input) {

        if (messagesEntered >= messagesAllowed) {
            System.out.println("You have already entered all " + messagesAllowed + " messages.");
            return;
        }

        while (messagesEntered < messagesAllowed) {

            System.out.println("\n==============================");
            System.out.println("MESSAGE " + (messagesEntered + 1) + " of " + messagesAllowed);
            System.out.println("==============================");

            // Recipient
            System.out.print("Enter recipient cell number: ");
            String recipient = input.nextLine();

            String recipientResult = checkRecipientCell(recipient);
            System.out.println(recipientResult);
            if (!login.checkCellPhoneNumber(recipient)) {
                continue;   // go back and ask for this message again
            }

            // Message text
            System.out.print("Enter your message: ");
            String messageText = input.nextLine();

            if (messageText.trim().isEmpty()) {
                System.out.println("The message cannot be empty.");
                continue;
            }
            if (messageText.length() > 250) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(checkMessageLength(messageText));
                continue;
            }
            System.out.println(checkMessageLength(messageText));

            // ID
            String messageID = generateMessageID();
            System.out.println(messageIDCreated(messageID));

            // Ask what to do with the message (keeps asking until the choice is 1, 2 or 3)
            int choice = 0;
            while (choice < 1 || choice > 3) {
                System.out.println("\nWhat do you want to do with this message?");
                System.out.println("1) Send Message");
                System.out.println("2) Disregard Message");
                System.out.println("3) Store Message to send later");
                System.out.print("Enter your choice: ");
                choice = input.nextInt();
                input.nextLine();

                if (choice < 1 || choice > 3) {
                    System.out.println(SentMessage(choice));
                }
            }

            // For choice 3 the message is only shown after the file was saved (see below)
            if (choice != 3) {
                System.out.println(SentMessage(choice));
            }

            if (choice == 1) {
                // Send
                addMessage(messageID, recipient, messageText, "Sent");
                System.out.println("\n" + messageDetails(messageIDs.size() - 1));

            } else if (choice == 2) {
                // Disregard: the message goes into the disregarded array
                int deleteChoice = input.nextInt();
                input.nextLine();
                if (deleteChoice == 0) {
                    System.out.println("Message deleted.");
                } else {
                    System.out.println("Message disregarded.");
                }
                addMessage(messageID, recipient, messageText, "Disregarded");

            } else {
                // Store: the message is saved in the JSON file
                boolean saved = addMessage(messageID, recipient, messageText, "Stored");
                if (saved) {
                    System.out.println(SentMessage(choice));
                } else {
                    System.out.println("Sorry, the message could not be saved to the file.");
                }
            }

            messagesEntered++;
        }

        System.out.println("\n===== All messages sent =====");
        System.out.println(printMessages());
        System.out.println("Total number of messages sent: " + returnTotalMessages());
    }

    // Adds a message to the arrays. The flag is "Sent", "Stored" or "Disregarded".
    // Returns false only if a stored message could not be saved to the file.
    public boolean addMessage(String messageID, String recipient, String messageText, String flag) {

        // The hash uses the position of the message (0 for the first message)
        String messageHash = createMessageHash(messageID, messageIDs.size(), messageText);

        // Every message goes into the "every message" arrays
        messageIDs.add(messageID);
        messageHashes.add(messageHash);
        messageRecipients.add(recipient);
        messageSenders.add(sender);
        messageTexts.add(messageText);
        messageFlags.add(flag);

        // ... and into the array for its flag
        boolean saved = true;
        if (flag.equals("Sent")) {
            sentMessages.add(messageText);
        } else if (flag.equals("Disregarded")) {
            disregardedMessages.add(messageText);
        } else if (flag.equals("Stored")) {
            storedMessages.add(messageText);
            saved = storeMessage();
        }
        return saved;
    }

    // Makes a random ten-digit ID that has not been used before
    public String generateMessageID() {
        String messageID;
        do {
            long number = 1000000000L + random.nextLong(9000000000L);
            messageID = String.valueOf(number);
        } while (messageIDs.contains(messageID) || !checkMessageID(messageID));

        return messageID;
    }

    // Checks that the message ID is not more than ten characters long
    public boolean checkMessageID(String messageID) {
        if (messageID == null) {
            return false;
        }
        return messageID.length() <= 10;
    }

    // Returns the message shown when a message ID has been made
    public String messageIDCreated(String messageID) {
        return "Message ID generated: " + messageID;
    }

    // Checks the recipient number by reusing checkCellPhoneNumber from Login
    // (+27 followed by 9 or 10 digits), and returns the messages from the brief
    public String checkRecipientCell(String cellphone) {
        if (login.checkCellPhoneNumber(cellphone)) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    // Checks that the message is not more than 250 characters
    public String checkMessageLength(String messageText) {
        if (messageText.length() <= 250) {
            return "Message ready to send.";
        } else {
            int extra = messageText.length() - 250;
            return "Message exceeds 250 characters by " + extra + "; please reduce the size.";
        }
    }

    // Makes the hash: first two digits of the ID, the message number, then the first and last word in capitals
    public String createMessageHash(String messageID, int messageNumber, String messageText) {

        String firstTwoNumbers = messageID.substring(0, 2);

        String[] words = messageText.trim().split("\\s+");

        // replaceAll removes anything that is not a letter or a number (like the ? in "tonight?")
        String firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "");

        String hash = firstTwoNumbers + ":" + messageNumber + ":" + firstWord + lastWord;

        return hash.toUpperCase();
    }

    // Returns the right message for the choice: 1 = send, 2 = disregard, 3 = store
    public String SentMessage(int choice) {
        if (choice == 1) {
            return "Message successfully sent.";
        } else if (choice == 2) {
            return "Press 0 to delete the message.";
        } else if (choice == 3) {
            return "Message successfully stored.";
        } else {
            return "Invalid option, please try again.";
        }
    }

    // Writes all the stored messages to the JSON file
    // Returns true if the file was saved and false if something went wrong
    // Reference: Google (n.d.) Gson User Guide. Available at: https://google.github.io/gson/UserGuide.html (Accessed: 18 November 2026).
    public boolean storeMessage() {

        JsonArray array = new JsonArray();

        // Go through every message and add the stored ones to the JSON array
        for (int i = 0; i < messageIDs.size(); i++) {
            if (messageFlags.get(i).equals("Stored")) {
                JsonObject messageObject = new JsonObject();
                messageObject.addProperty("messageID", messageIDs.get(i));
                messageObject.addProperty("messageHash", messageHashes.get(i));
                messageObject.addProperty("sender", messageSenders.get(i));
                messageObject.addProperty("recipient", messageRecipients.get(i));
                messageObject.addProperty("message", messageTexts.get(i));
                array.add(messageObject);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try {
            FileWriter writer = new FileWriter(storageFile);
            gson.toJson(array, writer);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Reads the stored messages from the JSON file into the arrays
    // Reference: Google (n.d.) Gson User Guide. Available at: https://google.github.io/gson/UserGuide.html (Accessed: 18 November 2026).
    public void readStoredMessagesFromFile() {

        File file = new File(storageFile);
        if (!file.exists()) {
            return;   // nothing has been stored yet
        }

        try {
            FileReader reader = new FileReader(file);
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            reader.close();

            for (int i = 0; i < array.size(); i++) {
                JsonObject messageObject = array.get(i).getAsJsonObject();
                String id = messageObject.get("messageID").getAsString();

                // Skip messages that are already in the arrays
                if (messageIDs.contains(id)) {
                    continue;
                }

                String text = messageObject.get("message").getAsString();

                String savedSender = "Unknown";
                if (messageObject.has("sender")) {
                    savedSender = messageObject.get("sender").getAsString();
                }

                messageIDs.add(id);
                messageHashes.add(messageObject.get("messageHash").getAsString());
                messageSenders.add(savedSender);
                messageRecipients.add(messageObject.get("recipient").getAsString());
                messageTexts.add(text);
                messageFlags.add("Stored");
                storedMessages.add(text);
            }
        } catch (Exception e) {
            System.out.println("The stored messages file could not be read.");
        }
    }

    // Returns the details of one message: ID, hash, recipient, message
    private String messageDetails(int index) {
        return "Message ID: " + messageIDs.get(index)
                + "\nMessage Hash: " + messageHashes.get(index)
                + "\nRecipient: " + messageRecipients.get(index)
                + "\nMessage: " + messageTexts.get(index) + "\n";
    }

    // Returns a report of all the messages that were sent (hash, recipient and message)
    public String printMessages() {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < messageIDs.size(); i++) {
            if (messageFlags.get(i).equals("Sent")) {
                report.append(messageDetails(i)).append("\n");
            }
        }
        if (report.length() == 0) {
            return "No messages have been sent yet.";
        }
        return report.toString();
    }

    // Returns how many messages have been sent
    public int returnTotalMessages() {
        return sentMessages.size();
    }

    // ----- Stored Messages menu -----

    // a. Shows the sender and recipient of every stored message
    public String displaySendersAndRecipients() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < messageIDs.size(); i++) {
            if (messageFlags.get(i).equals("Stored")) {
                result.append("Sender: ").append(messageSenders.get(i));
                result.append(" | Recipient: ").append(messageRecipients.get(i)).append("\n");
            }
        }
        if (result.length() == 0) {
            return "There are no stored messages.";
        }
        return result.toString();
    }

    // b. Finds the longest stored message
    public String longestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        String longest = storedMessages.get(0);
        for (int i = 1; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).length() > longest.length()) {
                longest = storedMessages.get(i);
            }
        }
        return longest;
    }

    // c. Searches for a message ID and returns its message
    public String searchByMessageID(String messageID) {
        int index = messageIDs.indexOf(messageID);
        if (index == -1) {
            return "Message ID not found.";
        }
        return messageTexts.get(index);
    }

    // c. Returns the recipient of the message with this ID
    public String getRecipientByMessageID(String messageID) {
        int index = messageIDs.indexOf(messageID);
        if (index == -1) {
            return "Message ID not found.";
        }
        return messageRecipients.get(index);
    }

    // d. Finds all the sent or stored messages for one recipient
    public ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < messageIDs.size(); i++) {
            boolean sentOrStored = messageFlags.get(i).equals("Sent") || messageFlags.get(i).equals("Stored");
            if (messageRecipients.get(i).equals(recipient) && sentOrStored) {
                found.add(messageTexts.get(i));
            }
        }
        return found;
    }

    // e. Deletes a message using its message hash
    public String deleteMessageByHash(String hash) {
        int index = messageHashes.indexOf(hash);
        if (index == -1) {
            return "Message hash not found.";
        }

        String text = messageTexts.get(index);
        String flag = messageFlags.get(index);

        // Take the message out of the array for its flag
        if (flag.equals("Sent")) {
            sentMessages.remove(text);
        } else if (flag.equals("Disregarded")) {
            disregardedMessages.remove(text);
        } else if (flag.equals("Stored")) {
            storedMessages.remove(text);
        }

        // Take it out of the "every message" arrays
        messageIDs.remove(index);
        messageHashes.remove(index);
        messageRecipients.remove(index);
        messageSenders.remove(index);
        messageTexts.remove(index);
        messageFlags.remove(index);

        // If it was a stored message, update the JSON file
        if (flag.equals("Stored")) {
            storeMessage();
        }

        return "Message: \"" + text + "\" successfully deleted.";
    }

    // f. Returns a report with the full details of all the stored messages
    public String storedMessagesReport() {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < messageIDs.size(); i++) {
            if (messageFlags.get(i).equals("Stored")) {
                report.append("Message ID: ").append(messageIDs.get(i)).append("\n");
                report.append("Message Hash: ").append(messageHashes.get(i)).append("\n");
                report.append("Sender: ").append(messageSenders.get(i)).append("\n");
                report.append("Recipient: ").append(messageRecipients.get(i)).append("\n");
                report.append("Message: ").append(messageTexts.get(i)).append("\n\n");
            }
        }
        if (report.length() == 0) {
            return "There are no stored messages.";
        }
        return report.toString();
    }

    // The menu for the Stored Messages option
    public void storedMessagesMenu(Scanner input) {
        int option;
        do {
            System.out.println("\n===== Stored Messages =====");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages for a recipient");
            System.out.println("5) Delete a message using its message hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Back to the main menu");
            System.out.print("Enter your choice: ");
            option = input.nextInt();
            input.nextLine();

            if (option == 1) {
                System.out.println(displaySendersAndRecipients());

            } else if (option == 2) {
                System.out.println(longestStoredMessage());

            } else if (option == 3) {
                System.out.print("Enter the message ID: ");
                String id = input.nextLine();
                String found = searchByMessageID(id);
                if (found.equals("Message ID not found.")) {
                    System.out.println(found);
                } else {
                    System.out.println("Recipient: " + getRecipientByMessageID(id));
                    System.out.println("Message: " + found);
                }

            } else if (option == 4) {
                System.out.print("Enter the recipient's cell number: ");
                String recipient = input.nextLine();
                ArrayList<String> found = searchByRecipient(recipient);
                if (found.isEmpty()) {
                    System.out.println("No messages found for that recipient.");
                } else {
                    for (int i = 0; i < found.size(); i++) {
                        System.out.println(found.get(i));
                    }
                }

            } else if (option == 5) {
                System.out.print("Enter the message hash: ");
                String hash = input.nextLine();
                System.out.println(deleteMessageByHash(hash));

            } else if (option == 6) {
                System.out.println(storedMessagesReport());

            } else if (option != 7) {
                System.out.println("Invalid option, please try again.");
            }
        } while (option != 7);
    }

}