# Secure Chat and File Transfer with Integrity and authentication (with attacks)

#### Scaftia.jar (in Scaftiaapp folder)
- double click Scaftia.jar  
- conect : click connect (the text changed to disconnect), need name and port.  
- disconnect :click the same btn (the+ text changed to connect) 
- add neighbor : need (ip,port) 
- remove: neighbor pres in list view and click remove
- edit setting : click edit (name, sharedpassword, macPassword, port) and click save
- send message : write message and click enter
- send file : chose neighbor from online list, choose file and click send (when its arrive with no problems show message in chat in format [filename to receiver], if failed  shows [filename to receiver  Failed!! ])
- receive message : shows in chat in format [from sender >  message] 
- receive file : Select a directory, shows in chat in format [from sender > filename] 
- send invalid message (invalid messages): select from combobox option and click send 
- send custom invalid message (custome invalid messages): select from combobox option write name or any text and click send

#### Key DistributionServer.jar (in DistributionServerApp folder)
- double DistributionServer.jar  
- start : click connect (the text changed to start), need to write port port.  
- stop : click the same btn (the text changed to connect) 
- add user : need (name, password) 
- send invalid responce (invalid responce): select from combobox option and click enable, the same btn is disable it.

---
 
#### Configuration files
- scaftia: use the appSetting.ini (username, passwords, port), Server.txt (include the server ip+port), neighbors.txt (neighbors ip+port)
- server: use the APP_SETTING.ini (port), USERS.txt (users name+password)

---

#### Needham-Schroeder
all the messges (4 => 10) encrepted with gcm, messages/errors saved in log file, this tool support for invalid or incorrect messages.

1. Request to server: [client1,client2,nonce(a)]\
client1 generate random number and send this message server.

2. responce fron server: [Ks,nonce(a),client2,token]\
server reponce decrepted message with client1 key, client1 check that nonce(a) and and client is that sended before.

3. client1 sends to client2: [token]\
this token is decrepted eith client2 key check that includes client1 name, remeber the token to use it.

4. client2 sends to client1: [nonce(b)]\
client2 generate random number, send this message with Ks. 

5. client1 sends to client2: [nonce(b)-1]\
this nonce(b) is decrepted with Ks, subtracts 1 from the nonce and send back.

6. client2 sends to client1: [ok)]\
client2 check if that nonce(b)-1+1 is the same number that sended, and send ok back.

#### messages (header,iv,content,hmac)  or  [header+" "+iv+" "+content+" "+hmac]
all the messages that arrives is decrypted by iv (header and content), create new hmac from the messages and verifies it with the incoming hmac.\
invaled messages saved just in log file.

1. HELLO:  [HELLO,IV,USERNAME,HMAC]\
the app check if this ip in neighbor list add it to online neighbors lis and send if find it send hello back.

2. BYE:  [BYE,IV,USERNAME,HMAC]\
the app remove this neighbor from online list.

3. MESSAGE:  [message,IV,USERNAME,HMAC]\
print the message in chat textArea with name of the sender [from sender >  message]

4. SENDFILE:  [SENDFILE,iv,FILENAME,HMAC]\
open dialog for accept or not the file, and send back OK or NO message.
if accept send ok with random port that listen to, if not send back no [NO,IV,NO,HMAC]

5. OK:  [OK,IV,PORT,HMAC]\
start sending the file  with this port (split the data and send in  chunks )

6. NO:  [NO,IV,NO,HMAC]\
cancel sending the file and show [Not interested] message in chat 

7. ACK:  [ACK,IV,ACK_FILE_(SUCCEED OR FAILED)-FILENAME,HMAC]\
SUCCEED: know that file is arrived, and decrypted successfully, view message in chat textArea.
FAILED: know that file is arrived, and decrypted failed or any chunk is missed, view message in chat textArea.

#### FILES (IV,header(ADD),FILEDATA+TAG)  or  [IV+" "+header(ADD)+" "+content+TAG]
all the data that arrives is decrypted with gcm and session key, sending back ack message.

---

#### Server 
- store user : the user data stores in the User.txt (username = password)
- client request: the sever  check if client1 and client exist in the users file, generate 128 random session key (used in the token, and outer message).
- server responce: server get client2 password encrept the session key+client1 with this password (this is token mssage), get client1 password and create outermessage (sessionkey,nonce(a),client2,token) encrept this message and send it back.

---

All the client out/in/error messages are saved in log.txt file, there is 4 types\.
- msg: [time and sate |send or receive |ip:port | name | header | content | iv | hmac | vailed or invaled]
- file: [time and sate |send or receive |ip:port | name | File | filename | iv ]
- bad msg: [time and sate |receive |ip:port | name | [ BAD {header} ]]
- error failed: [[time and sate |header |ip:port | name |  error]

All the server  messages are saved in log.txt file
- error failed: [time and sate |header |ip:port | sender |  recipient | nonce | is Encrypted |error | isSentBack | intentionally Incorrect]