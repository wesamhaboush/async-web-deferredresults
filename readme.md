* To run single thread handler:

            ab -k -n 1000 -c 1000 http://localhost:8082/echo/once/?text=1

* To run callable with threadpool workers (note the c added to 'once'):

            ab -k -n 1000 -c 1000 http://localhost:8082/echo/oncec/?text=1

* To run deferred result with a custom threadpool executor (note the d added to 'once'):

            ab -k -n 1000 -c 1000 http://localhost:8082/echo/onced/?text=1

* To run deferred result with disruptor workerpool (note the r added to 'once'):

            ab -k -n 1000 -c 1000 http://localhost:8082/echo/oncer/?text=1


* To start the server, right click the TomcatDriver class, and hit run in intellij
* You need java8 to run this project
* Note: tomcat has two connectors defined, one of them is the default, the other is NIO enabled, many connections-accepting 8082 ported connector. Use that in your tests to not be limited by the number of connections tomcat is accepting.
