# revolut-backend-test

## Purpose

The purpose of this project is to implement a REST api which handles money transfer between accounts.

## Build and deploy

In order to be able to build the application it is required to ***Java 11*** and the latest ***Maven*** installed.

### Building the application

```mnv package```

Since some of the functional tests may take a significant amount of time to run, we can skip them during deployment by
running the following command:

```mvn package -DskipTests=true```

## Running the application

```java -jar revolut-backend-test-1.0-SNAPSHOT.jar```

## General usage

In order to be able to make a transaction, first we have to register an user:

```
curl -X POST \
  http://localhost:8080/api/user \
  -H 'Content-Type: application/json' \
  -d '{
	"firstName": "asd",
	"lastName": "www",
	"address": "addr"
}'
```

*Response:* ```{"status":"SUCCESS","message":"Successfully created user with id 1"}```

Create a bank account for the user:

```
curl -X POST http://localhost:8080/api/account/1
```

*Response:* ```{"status":"SUCCESS","message":"Successfully created account with iban RO47XXXX5678901234567891"}```

Create another account for the user:

```
curl -X POST http://localhost:8080/api/account/1
```

*Response:* ```{"status":"SUCCESS","message":"Successfully created account with iban RO20XXXX5678901234567892"}```

Deposit some funds on the fist account:
 
```
curl -X PATCH \
  http://localhost:8080/api/account/deposit \
  -H 'Content-Type: application/json' \
  -d '{
	"iban": "RO47XXXX5678901234567891",
	"amount":1000
}'
```

*Response:* ```{"status":"SUCCESS","message":"Successfully deposited amount!"}```

Withdraw some amount from this account

```
curl -X PATCH \
  http://localhost:8080/api/account/withdraw \
  -H 'Content-Type: application/json' \
  -d '{
	"iban": "RO47XXXX5678901234567891",
	"amount":22
}'
```

*Response:* ```{"status":"SUCCESS","message":"Successfully withdrawn amount!"}```

Check amount on this account:

```curl -X GET http://localhost:8080/api/account/RO47XXXX5678901234567891```

*Response:* ```{"iban":"RO47XXXX5678901234567891","amount":978}```

Transfer amount to the second account:

```
curl -X PATCH \
  http://localhost:8080/api/account/transfer \
  -H 'Content-Type: application/json' \
  -d '{
	"senderIban": "RO47XXXX5678901234567891",
	"receiverIban": "RO20XXXX5678901234567892",
	"amount":500
}'
```

*Response:* ```{"status":"SUCCESS","message":"Successfully transferred amount!"}```

Check funds:

```curl -X GET http://localhost:8080/api/account/RO47XXXX5678901234567891```

*Response:* ```{"iban":"RO47XXXX5678901234567891","amount":478}```

```curl -X GET http://localhost:8080/api/account/RO20XXXX5678901234567892```

*Response:* ```{"iban":"RO20XXXX5678901234567892","amount":500}```

## Testing

The application contains unit tests for the business logic and functional tests for both the user api and bank account api.
Besides this there are some integration tests which aim to test the concurrency/multithreading approach of the application.
These tests aim to mimic a huge number of simultaneous parallel request ultimately trying to achieve information loss 
and/or deadlocks.

### Running the tests

```mvn test```


















