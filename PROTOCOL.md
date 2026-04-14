# Math Server Protocol Design (TCP)

## 1. Overview
This project uses **TCP (Transmission Control Protocol)** for communication between the client and server.

TCP was chosen because it provides:
- Reliable data delivery
- Ordered message transmission
- Persistent connections
- Built-in error handling

These features ensure that multiple clients can communicate correctly with the server and receive accurate responses.

---

## 2. Message Format
All messages are:
- Plain text
- One line per message
- Terminated by a newline character (`\n`)

---

## 3. Message Types

### JOIN (Client → Server)
**Format:**

JOIN:&lt;client name&gt;

**Example:**

JOIN:Alice

**Description:**
- Sent immediately after connecting
- Identifies the client to the server

---

### ACK (Server → Client)
**Format:**

ACK:JOINED:&lt;client name&gt;

**Example:**

ACK:JOINED:Alice


**Description:**
- Confirms successful connection
- Client must wait for this before sending any requests

---

### CALC (Client → Server)
**Format:**

CALC:&lt;expression&gt;

**Examples:**

- CALC:5 + 3
- CALC:10 / 2
- CALC:7 * 8

**Description:**
- Requests a mathematical calculation
- Supports: `+`, `-`, `*`, `/`

---

### RESULT (Server → Client)
**Format:**

RESULT:&lt;expression&gt;=&lt;answer&gt;

**Examples:**

- RESULT:5 + 3=8.0
- RESULT:10 / 2=5.0

**Description:**
- Returns the computed result of the expression

---

### ERROR (Server → Client)
**Format:**

ERROR:&lt;reason&gt;

**Examples:**

- ERROR:Division by zero
- ERROR:Invalid expression
- ERROR:Unknown command

**Description:**
- Indicates invalid input or failure

---

### QUIT (Client → Server)
**Format:**

QUIT

**Description:**
- Gracefully disconnects the client from the server

---

## 4. Connection Lifecycle

1. Client connects to the server using TCP
2. Client sends:

JOIN:&lt;clientName&gt;

3. Server responds:

ACK:JOINED:&lt;clientName&gt;

4. Client sends one or more:

CALC:&lt;expression&gt;

5. Server responds with:

RESULT:&lt;expression&gt;=&lt;answer&gt;

or:

ERROR:&lt;reason&gt;

6. Client sends:

QUIT

7. Server logs the session and closes the connection

---

## 5. Error Handling

- **Missing JOIN message:**

ERROR:Expected JOIN message

Connection is closed.

- **Invalid expression:**

ERROR:Invalid expression


- **Division by zero:**

ERROR:Division by zero


- **Unknown command:**

ERROR:Unknown command

---
test
