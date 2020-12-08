package db

import scalikejdbc._

class RuleManagerDB(url: String, user: String, password: String) {
  Class.forName("org.postgresql.Driver")
  ConnectionPool.singleton(url, user, password)

  def testConnection(): String = {
    DB localTx { implicit session =>
      sql""" SELECT 'HELLO WORLD' as hello_world """
        .map { _.string("hello_world") }
        .single()
        .apply()
        .get
    }
  }
}
