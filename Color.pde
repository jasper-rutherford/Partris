/*
 Final Project 5611
 Color Class
 
 Represents a color, because there's no color class and I want it to work how I want it to work.
 It's possible that this class already exists within processing in some format. Maybe it would work the same way.
 However, I was unable to find that exact preexisting functionality. So here it is.
 
 Written by Jasper Rutherford
 */

public class Color {
  //red, green, blue, alpha
  public int r;
  public int g;
  public int b;
  public int a;

  //constructor with no alpha specified. Alpha defaults to 255.
  public Color(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
    a = 255;
  }

  //constructor that specifies alpha
  public Color(int r, int g, int b, int a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }
  
  //returns a copy of this color
  public Color copy() {
     return new Color(r, g, b, a); 
  }
  
  //makes printing nicer
  public String toString() {
   return r + ", " + g + ", " + b + ", " + a; 
  }
  
  //checks if the two rgba's are the same
  public boolean equals(Color colour) {
     return this.r == colour.r 
         && this.g == colour.g 
         && this.b == colour.b 
         && this.a == colour.a; 
  }
}
