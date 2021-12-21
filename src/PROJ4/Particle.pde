/**
 Final Project 5611
 Partcle Class
 Represents one Particle
 
 Written by Jasper Rutherford
 */

public class Particle {
  //coordinates of top left corner of particle
  public float x;
  public float y;

  //coordinates in grid.particleGrid for this particle
  public int xIndex;
  public int yIndex;

  public String type;
  public Color colour;
  public String prevType;

  public int fuel;

  public boolean fresh;


  public boolean moved;
  public boolean interacted;
  public boolean awake;

  public Particle(String type, int xIndex, int yIndex) {
    this.xIndex = xIndex;
    this.yIndex = yIndex;

    x = grid.cornerX + xIndex * particleWidth;
    y = grid.cornerY + yIndex * particleWidth;

    this.type = type;
    this.colour = grid.colorMap.get(type).copy();
    this.prevType = type;

    //resetVariables();
    fresh = false;
    awake = true;
  }

  public void sleep() {
    awake = false;
  }

  public void wake() {
    moved = true;
    interacted = true;
    awake = true;
  }

  public void setType(String type) {
    //set the type
    prevType = this.type;
    this.type = type;
    this.colour = grid.colorMap.get(type).copy();

    //wake self
    wake();

    //wake neighbors
    ArrayList<Particle> neighbors = halfAdjacents();
    for (int lcv = 0; lcv < neighbors.size(); lcv++) {
      neighbors.get(lcv).wake();
    }

    //fuel any new fire
    if ((prevType.equals("Air") || prevType.equals("Plant")) && type.equals("Fire")) {
      fuel = baseFuel;
    } else if (prevType.equals("Charcoal") && type.equals("Fire")) {
      fuel = baseFuel * 3;
    } 

    //if this is a fire that burned out then gain a point
    if (!lost && prevType.equals("Fire") && type.equals("Air")) {
      score++;
    }

    fresh = true;
  }

  public void render() {
    if (!type.equals("Air")) {
      //Color test = colour.copy();
      //if (!awake && alphaSleep) {
      //  test.a /= 2;
      //}
      fill(colour);
      if (awake && alphaSleep) {
        strokeWeight(1);
      } else {
        strokeWeight(0);
      }
      rect(x, y, particleWidth, particleWidth);
      strokeWeight(1);
    }
  }

  public void move() {
    if (awake) {
      //tracks whether or not the particle moved
      moved = false;

      //move water
      if (type.equals("Water")) {
        //it checks 1. directly below, 2. the diagonals below, 3. the neighbors directly left and right. 
        //if 1 exists, move there
        //if 2 exists, move to one of those
        //if 3 exists, move to one of those
        ArrayList<Particle> openSpaces = new ArrayList<Particle>();

        //check lower neighbor particles
        Particle down = adjacentDown();
        if (down != null) {
          Particle downLeft = down.adjacentLeft();
          Particle downRight = down.adjacentRight();
          if (down.type.equals("Air")) {
            openSpaces.add(down);
          } else {
            if (downLeft != null && downLeft.type.equals("Air")) {
              openSpaces.add(downLeft);
            }
            if (downRight != null && downRight.type.equals("Air")) {
              openSpaces.add(downRight);
            }
          }
        }

        //only consider direct left and right neighbors if no lower neighbors are available
        if (openSpaces.size() == 0) {
          //check left neighbor particle
          Particle left = adjacentLeft();
          if (left != null && left.type.equals("Air")) {
            openSpaces.add(left);
          }

          //check right neighbor particle
          Particle right = adjacentRight();
          if (right != null && right.type.equals("Air")) {
            openSpaces.add(right);
          }
        }

        //pick a random spot from the openSpaces to move to
        if (openSpaces.size() != 0) {
          Particle move = openSpaces.get(int(random(0, openSpaces.size())));
          move.setType("Water");

          setType("Air");

          moved = true;
        }
      } 
      //move fire
      else if (type.equals("Fire")) {
        fuel -= int(random(0, baseFuel / 2));
        //colour.a = 255 * fuel / baseFuel;
        if (fuel <= 0) {
          setType("Air");
        }

        moved = true;
      }
      //move stone
      //move char
      else if (type.equals("Stone")) {
        //check the spots (including diagonals) below this particle for openness. 
        //if any of those spots are open, move to a random one of the spots
        //then choose a random open spot to move to. if none are found then there is no movement.
        ArrayList<Particle> openSpaces = new ArrayList<Particle>();

        //check lower neighbor particles
        Particle down = adjacentDown();
        if (down != null) {
          Particle downLeft = down.adjacentLeft();
          Particle downRight = down.adjacentRight();
          if (down.type.equals("Air") || down.type.equals("Water") || down.type.equals("Lava")) {
            openSpaces.add(down);
          } else {
            if (downLeft != null && (downLeft.type.equals("Air") || downLeft.type.equals("Water") || downLeft.type.equals("Lava"))) {
              openSpaces.add(downLeft);
            }
            if (downRight != null && (downRight.type.equals("Air") || downRight.type.equals("Water") || downRight.type.equals("Lava"))) {
              openSpaces.add(downRight);
            }
          }
        }

        //pick a random spot from the openSpaces to move to
        if (openSpaces.size() != 0) {
          Particle move = openSpaces.get(int(random(0, openSpaces.size())));
          String movedType = move.type;
          move.setType(type);

          setType(movedType);

          moved = true;
        }
      }
      //move lava
      else if (type.equals("Lava")) {
        //it checks 1. directly below, 2. the diagonals below, 3. the neighbors directly left and right. 
        //if 1 exists, move there
        //if 2 exists, move to one of those
        //if 3 exists, move to one of those
        ArrayList<Particle> openSpaces = new ArrayList<Particle>();

        //check lower neighbor particles
        Particle down = adjacentDown();
        if (down != null) {
          Particle downLeft = down.adjacentLeft();
          Particle downRight = down.adjacentRight();
          if (down.type.equals("Air")) {
            openSpaces.add(down);
          } else {
            if (downLeft != null && downLeft.type.equals("Air")) {
              openSpaces.add(downLeft);
            }
            if (downRight != null && downRight.type.equals("Air")) {
              openSpaces.add(downRight);
            }
          }
        }

        //only consider direct left and right neighbors if no lower neighbors are available
        if (openSpaces.size() == 0) {
          //check left neighbor particle
          Particle left = adjacentLeft();
          if (left != null && left.type.equals("Air")) {
            openSpaces.add(left);
          }

          //check right neighbor particle
          Particle right = adjacentRight();
          if (right != null && right.type.equals("Air")) {
            openSpaces.add(right);
          }
        }

        //pick a random spot from the openSpaces to move to
        if (openSpaces.size() != 0) {
          Particle move = openSpaces.get(int(random(0, openSpaces.size())));
          move.setType("Lava");

          setType("Air");

          moved = true;
        }
      }

      if ((type.equals("Fire") && !colour.equals(grid.colorMap.get("Fire"))) || (type.equals("Fire") && !colour.equals(grid.colorMap.get("Fire")))) {
        println("if this ever happens then somethings gone very wrong");
      }
    }
  }

  //get all particles that are adjacent to this particle, including diagonals
  public ArrayList<Particle> halfAdjacents() {
    //get all adjacent particles
    ArrayList<Particle> adjacents = new ArrayList<Particle>();

    //get left adjacent
    Particle adj = adjacentLeft();
    if (adj != null) {
      adjacents.add(adj);

      //try to get bottom left corner if left exists
      adj = adj.adjacentDown();
      if (adj != null) {
        adjacents.add(adj);
      }
    }

    //get right adjacent
    adj = adjacentRight();
    if (adj != null) {
      adjacents.add(adj);

      //try to get top right corner if right exists
      adj = adj.adjacentUp();
      if (adj != null) {
        adjacents.add(adj);
      }
    }

    //get up adjacent
    adj = adjacentUp();
    if (adj != null) {
      adjacents.add(adj);

      //try to get top left corner if up exists
      adj = adj.adjacentLeft();
      if (adj != null) {
        adjacents.add(adj);
      }
    }

    //get down adjacent
    adj = adjacentDown();
    if (adj != null) {
      adjacents.add(adj);

      //try to get bottom right corner if bottom exists
      adj = adj.adjacentRight();
      if (adj != null) {
        adjacents.add(adj);
      }
    } 
    return adjacents;
  }

  //returns particles that are adjacent to this particle, not including diagonals
  public ArrayList<Particle> fullAdjacents() {
    //get all adjacent particles
    ArrayList<Particle> adjacents = new ArrayList<Particle>();

    //get left adjacent
    Particle adj = adjacentLeft();
    if (adj != null) {
      adjacents.add(adj);
    }

    //get right adjacent
    adj = adjacentRight();
    if (adj != null) {
      adjacents.add(adj);
    }

    //get up adjacent
    adj = adjacentUp();
    if (adj != null) {
      adjacents.add(adj);
    }

    //get down adjacent
    adj = adjacentDown();
    if (adj != null) {
      adjacents.add(adj);
    } 

    return adjacents;
  }

  public void interact() {
    if (awake) {
      //track whether or not the particle interacts with anything. 
      interacted = false;

      ArrayList<Particle> halfAdjacents = halfAdjacents();
      ArrayList<Particle> fullAdjacents = fullAdjacents();
      Particle adj;

      //if the particle was created this step by another particle (probably in an interaction), then this particle does nothing this step
      if (fresh) {
        fresh = !fresh;
      } 
      //if it's not fresh though then it's good to go
      else {
        //interact fire
        if (type.equals("Fire")) {
          //check half adjacents for air. if there is no air then the fire goes out.
          boolean hasAir = false;
          for (int lcv = 0; lcv < halfAdjacents.size(); lcv++) {
            if (halfAdjacents.get(lcv).type.equals("Air")) {
              hasAir = true;
              break;
            }
          }

          //extinguish this fire if it has no air access (set to prevType if it has fuel) (which it should? it should never have zero fuel in this section.)
          if (!hasAir && fuel > 0) {
            setType(prevType);
            interacted = true;
          }
          //if the fire has no access to air and it has no fuel
          else if (!hasAir) {
            println("this really shouldnt be possible :/");
          }
          //if the fire has access to air then try to spread fire
          else {
            //loop through the fire's half adjacents
            for (int lcv = 0; lcv < halfAdjacents.size(); lcv++) {
              adj = halfAdjacents.get(lcv);

              //interact fire with plant
              if (adj.type.equals("Plant")) {
                //loop through the plant's half adjacents
                ArrayList<Particle> plantAdjacents = adj.halfAdjacents();
                for (int plantLcv = 0; plantLcv < plantAdjacents.size(); plantLcv++) {
                  //if the plant has any air fully adjacent
                  if (plantAdjacents.get(plantLcv).type.equals("Air")) {
                    //set the plant to be fire
                    adj.setType("Fire");
                    interacted = true;
                  }
                }
              }
              //interact fire with char
              if (adj.type.equals("Charcoal")) {
                //loop through the char's half adjacents
                ArrayList<Particle> charAdjacents = adj.halfAdjacents();
                for (int charLcv = 0; charLcv < charAdjacents.size(); charLcv++) {
                  //if the char has any air fully adjacent
                  if (charAdjacents.get(charLcv).type.equals("Air")) {
                    //set the char to be fire
                    adj.setType("Fire");
                    interacted = true;
                  }
                }
              }
              //interact fire with ice
              else if (adj.type.equals("Ice")) {
                //convert ice to water
                adj.setType("Water");
                interacted = true;
              }
            }
          }
        }
        //interact water
        else if (type.equals("Water")) {
          //check half adjacents
          for (int lcv = 0; lcv < halfAdjacents.size(); lcv++) {
            adj = halfAdjacents.get(lcv);
            if (adj.type.equals("Lava")) {
              adj.setType("Stone");
              setType("Stone");
              interacted = true;
            }
          }
        }
        //interact lava
        else if (type.equals("Lava")) {
          //check half adjacents
          for (int lcv = 0; lcv < halfAdjacents.size(); lcv++) {
            adj = halfAdjacents.get(lcv);
            //interact lava with water
            if (adj.type.equals("Water")) {
              adj.setType("Stone");
              setType("Stone");
              interacted = true;
            }
            //interact lava with plant 
            else if (adj.type.equals("Plant")) {
              //check plant's half adjacents for air
              ArrayList<Particle> plantAdjacents = adj.halfAdjacents();
              boolean hasAir = false;
              for (int plantLcv = 0; plantLcv < plantAdjacents.size(); plantLcv++) {
                if (plantAdjacents.get(plantLcv).type.equals("Air")) {
                  hasAir = true;
                  break;
                }
              }

              //if the plant has air fully adjacent
              if (hasAir) {
                //ignite the plant 
                adj.setType("Fire");
              }
              //if the plant has no air
              else {
                //convert to char
                adj.setType("Charcoal");
              }

              interacted = true;
            }
            //interact lava with char
            else if (adj.type.equals("Charcoal")) {
              //check char's half adjacents for air
              ArrayList<Particle> charAdjacents = adj.halfAdjacents();
              boolean hasAir = false;
              for (int charLcv = 0; charLcv < charAdjacents.size(); charLcv++) {
                if (charAdjacents.get(charLcv).type.equals("Air")) {
                  hasAir = true;
                  break;
                }
              }

              //if the char has air fully adjacent
              if (hasAir) {
                //ignite the char 
                adj.setType("Fire");
              }
              //if the char has no air
              else {
                //do nothing
              }

              interacted = true;
            }
            //interact lava with ice
            else if (adj.type.equals("Ice")) {
              //convert ice to water
              adj.setType("Water");
            }
          }
        }
        //interact plant
        else if (type.equals("Plant")) {
          //check all adjacent particles
          for (int lcv = 0; lcv < fullAdjacents.size(); lcv++) {
            adj = fullAdjacents.get(lcv);

            //if the adjacent particle is air, plant could grow there. 
            if (adj.type.equals("Air")) {
              //get all the particles adjacent to the air particles
              ArrayList<Particle> adjAdjacents = adj.halfAdjacents();

              //loop through these adjacent particles
              for (int lcv2 = 0; lcv2 < adjAdjacents.size(); lcv2++) {
                Particle adjAdj = adjAdjacents.get(lcv2);

                //if the particle isn't air or plant, then it is a viable spot to grow. 
                if (!adjAdj.type.equals("Air") && !adjAdj.type.equals("Plant")) {
                  //grow the plant
                  adj.setType("Plant");
                  interacted = true;

                  //plant cannot grow multiple times into one particle, so end the loop as soon as plant grows
                  break;
                }
              }
            }
          }
        }
        //interact ice
        else if (type.equals("Ice")) {
          //check full adjacents 
          for (int lcv = 0; lcv < halfAdjacents.size(); lcv++) {
            // check for water
            adj = halfAdjacents.get(lcv);
            if (adj.type.equals("Water")) {
              adj.setType("Ice");
              interacted = true;
            }
          }
        }
        //interact air 
        //this didn't end up getting into the game.
        else if (lost && type.equals("Air") && false) {
          ArrayList<Particle> adjacents;
          //randomly decide to use full or half neighbors
          //half adjacents
          if (int(random(0, 2)) == 0) {
            adjacents = halfAdjacents;
          } 
          //full adjacents
          else {
            adjacents = fullAdjacents;
          }

          //search adjacents for non air
          for (int lcv = 0; lcv < adjacents.size(); lcv++) {
            adj = adjacents.get(lcv);
            if (!adj.type.equals("Air")) {
              adj.setType("Air");
              score--;
            }
          }
        }

        if (!interacted && !moved) {
          sleep();
        }
      }
    }
  }

  //gets the particle from the grid that is to the left of this one
  public Particle adjacentLeft() {
    if (xIndex != 0) {
      return grid.particleGrid[xIndex - 1][yIndex];
    }
    return null;
  }

  //gets the particle from the grid that is to the right of this one
  public Particle adjacentRight() {
    if (xIndex != grid.particleGrid.length - 1) {
      return grid.particleGrid[xIndex + 1][yIndex];
    }
    return null;
  }

  //gets the particle from the grid that is above this one
  public Particle adjacentUp() {
    if (yIndex != 0) {
      return grid.particleGrid[xIndex][yIndex - 1];
    }
    return null;
  }

  //gets the particle from the grid that is below this one  
  public Particle adjacentDown() {
    if (yIndex != grid.particleGrid[0].length - 1) {
      return grid.particleGrid[xIndex][yIndex + 1];
    }
    return null;
  }

  //makes printing particles nicer
  public String toString() {
    return ("[" + xIndex + ", " + yIndex + ", " + type + ", " + awake + "]");
  }
}
