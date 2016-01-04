# Caesarchat

Client and Server components for a chat program with encrypted communication.
The encryption is a variation on the [caesar cipher](https://en.wikipedia.org/wiki/Caesar_cipher);
each character in a message is shifted by the numeric value of a character in the encryption key. 
The first character in the message is shifted by the first character in the key, the second character in 
the message to the second character in the key, and so on.  
If the encryption key is 'foobar', messages will be shifted by the values ``[102,111,111,98,97,114]``.


### TODO

 - [ ] Server features
  - [ ] Reconfigure server settings for password (and possibly port number) during run-time.
  - [ ] Manage connections, eg. view connection data and kick users
  - [ ] Log users and sent messages.
  - [ ] Blacklisting/whitelisting IPs
 - [ ] Client features
  - [ ] Let users pick names rather than the server giving each user a default name.
  - [ ] Menu option to connect/reconnect to server during run-time.
  - [ ] Chat commands, eg. whisper.
