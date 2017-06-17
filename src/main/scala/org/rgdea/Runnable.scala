package org.rgdea

object Runnable {

  def apply(f: => Unit): Runnable = new Runnable {
    override def run(): Unit = f
  }
}
