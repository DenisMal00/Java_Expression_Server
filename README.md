# MalasiDenis Server Project

## Overview

The MalasiDenis project is a Java server designed to process and solve mathematical expressions sent by clients. Utilizing a multi-threaded architecture to handle multiple requests simultaneously, the server offers efficient parallel processing of expressions. Each request is analyzed, computed, and the result is sent back to the client.

## Features

### Multi-threaded Processing
The server utilizes a multi-threading programming model to handle multiple client requests simultaneously. This ensures that the server can concurrently process various mathematical expressions, significantly improving throughput and reducing waiting times for clients. It employs the WorkerThread class to manage each client connection in a separate thread, thus enabling parallel and independent processing.

### Mathematical Expression Parsing
The parsing and computation system is designed to interpret and calculate complex mathematical expressions. It supports a variety of mathematical operators, including addition, subtraction, multiplication, division, and exponentiation, as well as recognizing parentheses for correct operation precedence. The parsing logic is located within the Parser and Operator classes, which work together to break down received expressions into simpler components and perform the required calculations.

### Exception Handling
Includes robust exception handling to address parsing errors, division by zero, and other computational errors.

## Prerequisites

- Java JDK 11 or higher

## Getting Started

To run the server, follow these steps:

1. Clone the repository or download the project.
2. Open the terminal or command prompt in the project directory.
3. Execute the command:
   java -jar MalasiDenis.jar
4. The server is now running and waiting for connections from clients.

## Structure

The project is organized as follows:

- `src/server`: Contains the source code of the main server and connection management.
- `src/server/computation`: Implements the parsing and computation system for mathematical expressions.
- `src/server/request`: Handles parsing of client requests.
- `src/server/exception`: Defines custom exceptions for error handling during parsing and computation.
