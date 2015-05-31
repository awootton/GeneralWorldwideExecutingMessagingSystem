import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BOARD)

thePin = 11

GPIO.setup(thePin, GPIO.IN, pull_up_down = GPIO.PUD_UP)

while True:

        if(GPIO.input(thePin) ==1):

                print("Button pressed")
        else:
                print("button NOT ")
GPIO.cleanup()

