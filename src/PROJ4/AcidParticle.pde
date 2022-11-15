public class AcidParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(226, 255, 97);

  private int health = 1;
  private int consumeCost = 1;

  public AcidParticle(Point indices, String prevType)
  {
    super(indices, "Acid", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {
    //it checks 1. directly below, 2. the diagonals below, 3. the neighbors directly left and right. 
    //if 1 exists (and is not acid), move there
    //if 2 exists (and is not acid), move to one of those
    //if 3 exists (and is not acid), move to one of those

    //create a list of particles which this particle can move into
    ArrayList<Particle> openSpaces = new ArrayList<Particle>();

    //get particle which is below this particle
    Particle down = adjacentDown();

    //if that particle is not null
    if (down != null) 
    {
      //if the particle below this one is not acid or stone
      if (!down.type.equals("Acid") && !down.type.equals("Stone")) 
      {
        //add that particle to the list of spaces to move into
        openSpaces.add(down);
      } 

      //if no space has been found
      if (openSpaces.size() == 0)
      {
        //check the diagonals 

        //get the diagonal particles
        Particle downLeft = down.adjacentLeft();
        Particle downRight = down.adjacentRight();

        //check the left diagonal for non-acid
        if (downLeft != null && !downLeft.type.equals("Acid") && !downLeft.type.equals("Stone")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downLeft);
        }

        //check the right diagonal for non-acid
        if (downRight != null && !downRight.type.equals("Acid") && !downRight.type.equals("Stone")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downRight);
        }
      }
    }

    //if no spaces have been found
    if (openSpaces.size() == 0)
    {
      //check the left/right neighbors

      //get left/right neighbor particles
      Particle left = adjacentLeft();
      Particle right = adjacentRight();

      //check the left neighbor for non-acid
      if (left != null && !left.type.equals("Acid") && !left.type.equals("Stone")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(left);
      }

      //check the right neighbor for non-acid
      if (right != null && !right.type.equals("Acid") && !right.type.equals("Stone")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(right);
      }
    }

    //if any potential particles have been found to move into
    if (openSpaces.size() != 0) 
    {
      //choose a random available particle
      Particle rand = openSpaces.get(int(random(0, openSpaces.size())));

      //dissolve the particle
      rand = dissolve(rand);

      //if the acid still has health
      if (health > 0) //(doesnt have to kill the acid here, it dies in the dissolve method)
      {
        //swap this particle with the dissolved particle
        grid.swapParticles(this, rand);

        //mark that this particle moved
        moved = true;
      }
    }
  }

  public Particle dissolve(Particle particle)
  {
    //if the particle is not air (air doesnt dissolve)
    if (!particle.type.equals("Air") && !particle.type.equals("Stone"))
    {
      //create a new air particle
      Particle air = particleFactory.generateParticle("Air", particle.type, particle.indices);

      //replace the found particle with the air particle
      grid.replaceParticle(particle, air);

      //consume acid health
      health -= consumeCost;

      //if no more health
      if (health <= 0)
      {
        //create a new air particle
        Particle newAir = particleFactory.generateParticle("Air", "Acid", particle.indices);

        //replace this particle with the new air particle
        grid.replaceParticle(this, newAir);
      }

      //return the air that is in the dissolved spot
      return air;
    }

    //return the air
    return particle;
  }

  public void interact()
  {
    //get the above particle
    Particle up = adjacentUp();

    //check if it's not acid or air
    if (up != null && !up.type.equals("Air") && !up.type.equals("Acid"))
    {
      //dissolve it
      dissolve(up);
    }
  }
}
