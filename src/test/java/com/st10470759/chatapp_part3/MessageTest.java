/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.st10470759.chatapp_part3;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rnndo
 */
public class MessageTest {

    private Message msg;

    // The stored messages in the tests go into this file, not the real one
    private static final String TEST_FILE = "testStoredMessages.json";

    // Runs before every test so each test starts with a fresh Message object
    @BeforeEach
    public void setUp() {
        msg = new Message();
        msg.setStorageFile(TEST_FILE);
    }

    // Runs after every test and removes the test file
    @AfterEach
    public void tearDown() {
        new File(TEST_FILE).delete();
    }

    // Runs the two test messages through sendingMessages.
    // Message 1 is sent and message 2 is disregarded (typed in for us, one line at a time).
    // 08575975889 is rejected by the recipient check, so message 2 uses a valid number here.
    private void runTestMessages() {
        String typedInput = "+27718693002\n"
                + "Hi Mike, can you join us for dinner tonight?\n"
                + "1\n"
                + "+27825975889\n"
                + "Hi Keegan, did you receive the payment?\n"
                + "2\n"
                + "0\n";
        Scanner input = new Scanner(typedInput);
        msg.setMessagesAllowed(2);
        msg.sendingMessages(input);
    }

    // ===================== PART 2 TESTS =====================

    // Message is not more than 250 characters (success)
    @Test
    public void testMessageLength_Success() {
        System.out.println("checkMessageLength - success");
        String text = "Hi Mike, can you join us for dinner tonight?";
        assertEquals("Message ready to send.", msg.checkMessageLength(text));
    }

    // Message is more than 250 characters (failure)
    // "a".repeat(255) makes a message of 255 letters, which is 5 too many
    @Test
    public void testMessageLength_Failure() {
        System.out.println("checkMessageLength - failure");
        String text = "a".repeat(255);
        assertEquals("Message exceeds 250 characters by 5; please reduce the size.", msg.checkMessageLength(text));
    }

    // Recipient number is correctly formatted (success)
    @Test
    public void testRecipientCell_Success() {
        System.out.println("checkRecipientCell - success");
        assertEquals("Cell phone number successfully captured.", msg.checkRecipientCell("+27718693002"));
    }

    // Recipient number is incorrectly formatted (failure)
    @Test
    public void testRecipientCell_Failure() {
        System.out.println("checkRecipientCell - failure");
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.", msg.checkRecipientCell("08575975889"));
    }

    // Message hash is correct for Test Case 1
    // We pass in our own ID because the hash needs an ID that starts with 00
    @Test
    public void testMessageHash_TestCase1() {
        System.out.println("createMessageHash - test case 1");
        String id = "0012345678";
        String text = "Hi Mike, can you join us for dinner tonight?";
        assertEquals("00:0:HITONIGHT", msg.createMessageHash(id, 0, text));
    }

    // Tests the other message hashes in a loop
    @Test
    public void testMessageHash_AllMessages() {
        System.out.println("createMessageHash - loop");

        // Each array holds one piece of test data. Position 0 goes together, position 1 goes together, and so on
        String[] ids = {"0012345678", "1212345678", "5512345678"};
        int[] numbers = {0, 1, 2};
        String[] texts = {
            "Hi Mike, can you join us for dinner tonight?",
            "Hi Keegan, did you receive the payment?",
            "Hi Sam, many thanks!"
        };
        String[] expected = {
            "00:0:HITONIGHT",
            "12:1:HIPAYMENT",
            "55:2:HITHANKS"
        };

        // The loop goes through every position and checks the hash for that message
        for (int i = 0; i < texts.length; i++) {
            String result = msg.createMessageHash(ids[i], numbers[i], texts[i]);
            assertEquals(expected[i], result);
        }
    }

    // A message ID is created: it has 10 digits and the message says "Message ID generated: <ID>"
    @Test
    public void testMessageID_Created() {
        System.out.println("generateMessageID");
        String id = msg.generateMessageID();
        assertEquals("Message ID generated: " + id, msg.messageIDCreated(id));
        assertEquals(10, id.length());
        assertTrue(msg.checkMessageID(id));
    }

    // A ten-character ID is allowed
    @Test
    public void testCheckMessageID_Valid() {
        System.out.println("checkMessageID - valid");
        assertTrue(msg.checkMessageID("1234567890"));
    }

    // An ID longer than ten characters is not allowed
    @Test
    public void testCheckMessageID_Invalid() {
        System.out.println("checkMessageID - invalid");
        assertFalse(msg.checkMessageID("12345678901"));
    }

    // User selected 'Send Message'
    @Test
    public void testSentMessage_Send() {
        System.out.println("SentMessage - send");
        assertEquals("Message successfully sent.", msg.SentMessage(1));
    }

    // User selected 'Disregard Message'
    @Test
    public void testSentMessage_Disregard() {
        System.out.println("SentMessage - disregard");
        assertEquals("Press 0 to delete the message.", msg.SentMessage(2));
    }

    // User selected 'Store Message'
    @Test
    public void testSentMessage_Store() {
        System.out.println("SentMessage - store");
        assertEquals("Message successfully stored.", msg.SentMessage(3));
    }

    // Message 1 is sent and message 2 is disregarded, so the total sent is 1
    @Test
    public void testReturnTotalMessages() {
        System.out.println("returnTotalMessages");
        runTestMessages();
        assertEquals(1, msg.returnTotalMessages());
    }

    // Only the sent message is listed, the disregarded one is not
    @Test
    public void testPrintMessages() {
        System.out.println("printMessages");
        runTestMessages();
        String result = msg.printMessages();
        assertTrue(result.contains("Hi Mike, can you join us for dinner tonight?"));
        assertTrue(result.contains("+27718693002"));
        assertFalse(result.contains("Keegan"));
    }

    // ===================== PART 3 TESTS =====================

    // Developer entry for test data messages 1 to 4 (no typing needed)
    private void addTestMessages1To4() {
        msg.addMessage(msg.generateMessageID(), "+27834557896", "Did you get the cake?", "Sent");
        msg.addMessage(msg.generateMessageID(), "+27838884567", "Where are you? You are late! I have asked you to be on time.", "Stored");
        msg.addMessage(msg.generateMessageID(), "+27834484567", "Yohoooo, I am at your gate.", "Disregarded");
        msg.addMessage(msg.generateMessageID(), "0838884567", "It is dinner time !", "Sent");
    }

    // Sent messages array contains the expected test data
    @Test
    public void testSentMessagesArray() {
        System.out.println("sentMessages array");
        addTestMessages1To4();
        List<String> expected = List.of("Did you get the cake?", "It is dinner time !");
        assertEquals(expected, msg.sentMessages);
    }

    // Display the longest message
    @Test
    public void testLongestMessage() {
        System.out.println("longestStoredMessage");
        addTestMessages1To4();
        assertEquals("Where are you? You are late! I have asked you to be on time.", msg.longestStoredMessage());
    }

    // Search for a message ID (message 4)
    @Test
    public void testSearchByMessageID() {
        System.out.println("searchByMessageID");
        addTestMessages1To4();
        // the ID is random, so we read it from the array (message 4 is at position 3)
        String id = msg.messageIDs.get(3);
        assertEquals("It is dinner time !", msg.searchByMessageID(id));
        assertEquals("0838884567", msg.getRecipientByMessageID(id));
    }

    // Search all the messages sent or stored for one recipient
    @Test
    public void testSearchByRecipient() {
        System.out.println("searchByRecipient");
        addTestMessages1To4();
        msg.addMessage(msg.generateMessageID(), "+27838884567", "Ok, I am leaving without you.", "Stored");

        List<String> expected = List.of("Where are you? You are late! I have asked you to be on time.", "Ok, I am leaving without you.");
        assertEquals(expected, msg.searchByRecipient("+27838884567"));
    }

    // Delete a message using its message hash (test message 2)
    @Test
    public void testDeleteMessageByHash() {
        System.out.println("deleteMessageByHash");
        addTestMessages1To4();
        // the hash is read from the array (message 2 is at position 1)
        String hash = msg.messageHashes.get(1);
        assertTrue(hash.endsWith(":1:WHERETIME"));

        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.", msg.deleteMessageByHash(hash));
        assertEquals(3, msg.messageIDs.size());
        assertTrue(msg.storedMessages.isEmpty());
    }

    // Display report: all the sent messages with the hash, recipient and message
    @Test
    public void testDisplayReport() {
        System.out.println("printMessages - report");
        addTestMessages1To4();
        String report = msg.printMessages();

        // message 1
        assertTrue(report.contains(msg.messageHashes.get(0)));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
        // message 4
        assertTrue(report.contains(msg.messageHashes.get(3)));
        assertTrue(report.contains("0838884567"));
        assertTrue(report.contains("It is dinner time !"));
        // the stored and disregarded messages are not in the report
        assertFalse(report.contains("Where are you?"));
        assertFalse(report.contains("Yohoooo"));
        assertEquals(2, msg.returnTotalMessages());
    }

}