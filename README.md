<h1>CS441 Fall 2024, Building LLM from Scratch | Homework 2</h1>

Things to do before running project:
- Compile project using "sbt clean compile".
- Check resources/application.conf to make sure input and output files are correctly set up.

How to run project:
- Type "sbt run" in the console/terminal.
- Type "sbt test" to run all unit tests.

Expected output:
- The following files will be produced in this order: part-00000, answer, trained_model.zip.
- All files produced can be found in the output folder (except for test cases).

How the project works:
- Main.scala will be the starting point of the project, where it will contain information on input and outputs.
- Main.scala will call Tokenization and Embedding accordingly, so you do not need to do anything.
- When Tokenization.scala is called, the input file is passed into the class and it will split the input file into shards and tokenize it into integers.
- The file produced by Tokenization.scala (part-00000) is then cleaned up in Main.scala, and the cleaned version will replace the contents of part-00000.
- The clean part-000000 will then be passed to Embedding.scala, where Word2Vec will read part-00000's contents and convert it into token embeddings.
- The output of Word2Vec will then be placed in a file called answer.
- This file will then be used in SlidingWindow.scala.
- Inside SlidingWindow.scala, the token embeddings created from Word2Vec will go through the SlidingWindow algorithm, then converted into RDD.
- The model is then trained and saved inside trained_model.zip.
