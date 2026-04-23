#include <Servo.h>

Servo myServo;

const int servoPin = 9;
int angle = 90;

void setup() {
  Serial.begin(9600);

  myServo.attach(servoPin);
  myServo.write(angle);

  delay(500);

  Serial.println("Servo Controller Ready");
  Serial.println("Send: A:0-180");
}

void loop() {

  if (Serial.available() > 0) {

    String data = Serial.readStringUntil('\n');

    data.trim();

    if (data.startsWith("A:")) {

      int receivedAngle = data.substring(2).toInt();

      if (receivedAngle >= 0 && receivedAngle <= 180) {
        angle = receivedAngle;
        myServo.write(angle);

        Serial.print("Servo moved to: ");
        Serial.println(angle);
      } else {
        Serial.println("Invalid angle! Use 0-180");
      }

    } else {
      Serial.println("Unknown command");
    }
  }
}