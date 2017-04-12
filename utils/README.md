## Test script
###### Environment: python3

#### Sample script
##### Test correct result, use:

    > python grader.py app.log
    
    [Trace] Processing log file: app.log
    Congratulations! Your algorithm is correct :)
    
##### Test invalid result, use:

    > python grade.py app-mismatch.log
    
    [Trace] Processing log file: app-mismatch.log
    [Error] Critical section execution mismatch
    at line 2:
           [Node 0] Enter critical section
           [Node 1] Leave critical section
