package org.cs441homework2.com
package ScalaTest

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import Main.{deleteDirectory, removeKeyFromFile}

import scala.io.Source._
import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}
import scala.io.Source

class MyScalaTest extends AnyFunSuite with BeforeAndAfterAll {
  val filePath = "src/test/test.txt"

  test("Verify deleteDirectory") {
    val writer = new PrintWriter(new File(filePath))

    try {
      writer.write("testing purposes only.")
    } finally {
      writer.close()
    }

    assert(Files.exists(Paths.get(filePath)), "File should exist")

    deleteDirectory(filePath)

    assert(!Files.exists(Paths.get(filePath)), "Expected file to be deleted")
  }

  test("Verify removeKeyFromFile") {
    val writer = new PrintWriter(new File(filePath))

    try {
      writer.write(" 123 123 123 123 123 123 123 123\n   123 123 123 123 123 123 123 123")
    } finally {
      writer.close()
    }

    assert(Files.exists(Paths.get(filePath)), "File should exist")

    removeKeyFromFile(filePath)

    val source = Source.fromFile(filePath)
    try {
      val content:String = source.getLines.mkString("\n")

      val answer = "123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 1"

      assert(answer==content, "Key should be removed from each line")
    } finally {
      source.close()
    }
  }

  test("Verify removeKeyFromFile with deleteDirectory") {
    val writer = new PrintWriter(new File(filePath))

    try {
      writer.write(" 123 123 123 123 123 123 123 123\n   123 123 123 123 123 123 123 123")
    } finally {
      writer.close()
    }

    assert(Files.exists(Paths.get(filePath)), "File should exist")

    removeKeyFromFile(filePath)

    val source = Source.fromFile(filePath)
    try {
      val content:String = source.getLines.mkString("\n")

      val answer = "123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 1"

      assert(answer==content, "Key should be removed from each line")
    } finally {
      source.close()
    }

    deleteDirectory(filePath)
    assert(!Files.exists(Paths.get(filePath)), "Expected file to be deleted")
  }

  test("deleteDirectory on non-existent file") {
    // Running deleteDirectory shouldn't crash the test case
    deleteDirectory(filePath)

    assert(!Files.exists(Paths.get(filePath)), "Test case shouldn't crash")
  }

  test("removeKeyFromFile with weird input") {
    val writer = new PrintWriter(new File(filePath))

    try {
      writer.write("                                             123 123 123 123 123 123 123 123\n                                           123 123 123 123 123 123 123 123")
    } finally {
      writer.close()
    }

    assert(Files.exists(Paths.get(filePath)), "File should exist")

    removeKeyFromFile(filePath)

    val source = Source.fromFile(filePath)
    try {
      val content:String = source.getLines.mkString("\n")

      val answer = "123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 1"

      assert(answer==content, "Key should be removed from each line")
    } finally {
      source.close()
    }
  }

}
