# Detailed Instructions on Running the Program
- Download the source code
- Open the program in an IDE (optional)
- Compile the code
- Start the server
- Start the client in another terminal
- Send and receive messages

# Description of the Program
### Siguria-e-te-dhenave-Detyra-3.iml
- Specifies module settings and source folder URLs
### misc.xml:
- Defines project version, JDK settings, and output directory.
### modules.xml:
- Specifies module file URL and path within the project.
### vcs.xml:
- Maps the project's root directory to Git for version control.
### RSAKeyPairGenerator.java
- This Java class RSAKeyPairGenerator provides a method generateKeyPair() to create an RSA key pair of length 1024 bits using a secure random number generator.
### Server.java
- This Java code defines a server that uses RSA encryption to securely exchange messages with clients over a network. - It listens for incoming connections on port 12345 and handles them in a multi-threaded manner.
-  The server generates its RSA key pair and sends its public key to clients for secure communication.
-  Each client connection is handled by a separate thread (ClientHandler) that receives and decrypts messages from clients using the server's private key.
-  The server sends its public key to clients and receives their public keys for secure communication.
### Client.java
- The Client class is part of a client-server system that uses RSA encryption for secure communication. Here's a more detailed explanation of its functionality: 
- The client generates its RSA key pair using the RSAKeyPairGenerator class.
- Upon establishing the connection, the client receives the server's public key from the server via an ObjectInputStream.
- The client then sends its own public key to the server using an ObjectOutputStream.
- The client prompts the user to enter a message through the console.
- It encrypts the message using the server's public key and the encryptMessage method, which uses the Cipher class to perform the encryption and Base64 encoding to convert the encrypted bytes into a string.
- The encrypted message is sent to the server using the same ObjectOutputStream used for the key exchange.
- After sending the encrypted message, the client outputs a confirmation message to the console, indicating that the message has been successfully encrypted and sent to the server.





