<h1>CS441 Fall 2024, Building LLM from Scratch | Homework 3</h1>

Things to do before running project:
- Compile project using "sbt clean compile".
- Check resources/application.conf to make sure input and output files are correctly set up.

How to run project:
- Type "sbt run" in the console/terminal.
- The program will prompt you to choose between Option 1 (Create and Train Model) and Option 2 (Run Akka Server with Model)
- After running Option 2, please go to Postman and make a POST request to http://localhost:8080/api/user
- Please include a body of JSON format in the HTTP Request. Below is an example of a valid HTTP request to my Akka implementation. Note that only the query parameter is accepted in the model.

$ POST http://localhost:8080/api/user\n
Body content (JSON format): {"query": "love is in the air"}

How to run tests:
- Type "sbt test" to run all unit tests.

Expected output:
- The following files will be produced in this order: part-00000, answer, trained_model.zip.
- All files produced can be found in the output folder (except for test cases).
- When generating sentences, due to my poor implementation of creating and training my model, the sentence generated will always be something that looks like "eighteighteighteight" or "outputoutputoutputoutput". This is most likely due to the model not being able to learn the dataset properly.

How the project (model implementation) works:
- Main.scala will be the starting point of the project, where it will contain information on input and outputs.
- Main.scala will call Tokenization and Embedding accordingly, so you do not need to do anything.
- When Tokenization.scala is called, the input file is passed into the class and it will split the input file into shards and tokenize it into integers.
- The file produced by Tokenization.scala (part-00000) is then cleaned up in Main.scala, and the cleaned version will replace the contents of part-00000.
- The clean part-000000 will then be passed to Embedding.scala, where Word2Vec will read part-00000's contents and convert it into token embeddings.
- The output of Word2Vec will then be placed in a file called answer.
- This file will then be used in SlidingWindow.scala.
- Inside SlidingWindow.scala, the token embeddings created from Word2Vec will go through the SlidingWindow algorithm, then converted into RDD.
- The model is then trained and saved inside trained_model.zip.

How the project (server implementation) works:
- WebServer.scala can be seen as the starting point of the server's code. It specifies the routes that the API will contain, and the port it is listening to.
- When a user makes an HTTP request to the Akka server, RouteConfig.scala will handle the POST request and its implementation (refer to example above for POST request).
- The user's query is received, and is then sent to GenerateSentence.scala for sentence generation.
- The model returns a generated sentence and is sent back via the RESTful API and can be seen on Postman's interface.
