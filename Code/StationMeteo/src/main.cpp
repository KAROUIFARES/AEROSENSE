#include <Arduino.h>
#include <DHT.H>
#include <LiquidCrystal.h>
int DHTPIN = 21;
#define DHTTYPE DHT11 // Definit le type de capteur utilise

DHT dht(DHTPIN, DHTTYPE);
LiquidCrystal lcd(27, 26, 25, 33, 32, 14);

void setup()
{
  lcd.begin(16, 2);
  dht.begin();

  lcd.print("AEROSENSE...");
  delay(3000);
}

void loop()
{
  lcd.setCursor(0, 0);
  lcd.print("Temp : " + String(dht.readTemperature()) + "°C");

  lcd.setCursor(0, 1);
  lcd.print("Hum : " + String(dht.readHumidity()) + "%");
}
