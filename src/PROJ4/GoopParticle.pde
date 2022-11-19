import java.util.Collections;

public class GoopParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(173, 23, 207);
  private boolean didStuff = false;

  public GoopParticle(Point indices, String prevType)
  {
    super(indices, "Goop", prevType);
    this.colour = uniqueColor;
  }

  //move goop
  // check each space around this one
  // the air space that has the most goop around it is the space to move to
  // ties are chosen by random
  public void move()
  {
    //list of spaces to move into
    ArrayList<Particle> spaces = new ArrayList<Particle>();

    //number of goop nearby each best available space
    int bestGoopCount = 0;

    //get all the neighbor particles
    ArrayList<Particle> neighbors = halfAdjacents();

    //check all neighbors
    for (Particle neighbor : neighbors)
    {
      //if the neighbor is air
      if (neighbor.type.equals("Air"))
      {
        //get the neighbor's goop count
        int goopCount = calcGoopCount(neighbor);
        

        if (goopCount > bestGoopCount)
        {
          bestGoopCount = goopCount;
          spaces = new ArrayList<Particle>();
          spaces.add(neighbor);
        }
        else if (goopCount == bestGoopCount)
        {
          spaces.add(neighbor);
        }
      }
    }

    //only move if there is one best space
    if (spaces.size() == 1)
    {
      //choose a random particle from the list of spaces
      Particle chosen = spaces.get(int(random(0, spaces.size())));

      //switch this particle with that particle
      grid.swapParticles(chosen, this);

      //you dont have to mark this particle as moved - grid.swapparticles does that automatically. all the other move methods were written before this functionality existed/before I realized it was redundant
    }

    doStuff();
  }

  //calculate how many of the particles surrounding this particle are goop or were goop 
  private int calcGoopCount(Particle p)
  {
    int goopCount = 0;
    for (Particle adj : p.wideNeighbors((particlesPerEdge + 1) / 2))
    {
      if (adj.type.equals("Goop"))
      {
        goopCount++;
      }
      else if (!adj.type.equals("Air"))
      {
        goopCount += 5;
      }
    }

    return goopCount;
  }

  //loop through full adjacents in random order
  //if the adj is not air or goop, replace this goop with another particle of adj
  public void interact()
  {
    // if (!didStuff)
    // {
    //   doStuff();
    // }
  }

  private void doStuff()
  {
    //get neighbors    
    ArrayList<Particle> neighbors = fullAdjacents();

    //randomize order
    Collections.shuffle(neighbors);

    //loop through neighbors
    for (Particle adj : neighbors)
    {
      //if adj isnt air or goop
      if (!adj.type.equals("Air") && !adj.type.equals("Goop"))
      {
        //replace this particle with a new particle of adj.type
        grid.replaceParticle(this, adj.type);

        //stop looping through neighbors
        break;
      }
    }
  }
}
