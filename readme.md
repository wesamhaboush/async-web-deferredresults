* this command should perform better with the deferred, and worse for the non-deferred:


            ab -k -n 1000 -c 400 http://localhost:8081/echo/once/?text=1
