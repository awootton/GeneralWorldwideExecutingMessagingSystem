#!/usr/bin/env python

import websocket
import thread
import time

running = 1

def on_message(ws, message):
    print message

def on_error(ws, error):
    running = 0
    print "had error {}".format(error)

def on_close(ws):
    running = 0
    print "### closed ### {}".format(ws)

def on_open(ws):
    def run(*args):
        for i in range(3):
            time.sleep(1)
            ws.send("{\"@\":\"gwems.Ping\"}")
        time.sleep(1)
        ws.send("{\"@\":\"gwems.Subscribe\",\"channel\":\"#TimeEveryMinute\"}")
        # ws.close()
        # print "thread terminating..."
        while running:
            time.sleep(60)
            ws.send("{\"@\":\"Live\"}")
    thread.start_new_thread(run, ())

if __name__ == "__main__":
    websocket.enableTrace(True)
    ws = websocket.WebSocketApp("ws://localhost:8081/",
                                on_message = on_message,
                                on_error = on_error,
                                on_close = on_close)
    ws.on_open = on_open

    ws.run_forever()
    
    print "the end of the world"