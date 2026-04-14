# Math Server Protocol Design (TCP)

## 1. Overview
This project uses **TCP (Transmission Control Protocol)** for communication between the client and server

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
