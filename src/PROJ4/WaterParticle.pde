
//represents a particle of water
public class WaterParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(66, 135, 245);

  public WaterParticle(Point indices, String prevType)
  {
      super(indices, "Water", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {
    //it checks 1. directly below, 2. the diagonals below, 3. the neighbors directly left and right. 
    //if 1 exists, move there
    //if 2 exists, move to one of those
    //if 3 exists, move to one of those

    //create a list of particles which this particle can move into
    ArrayList<Particle> openSpaces = new ArrayList<Particle>();

    //get particle which is below this particle
    Particle down = adjacentDown();

    //if that particle is not null
    if (down != null) 
    {
      //if the particle below this one is air
      if (down.type.equals("Air")) 
      {
        //add that particle to the list of spaces to move into
        openSpaces.add(down);
      } 

      //if no space has been found
      if (openSpaces.size() == 0)
      {
        //check the diagonals for air

        //get the diagonal particles
        Particle downLeft = down.adjacentLeft();
        Particle downRight = down.adjacentRight();

        //check the left diagonal for air
        if (downLeft != null && downLeft.type.equals("Air")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downLeft);
        }

        //check the right diagonal for air
        if (downRight != null && downRight.type.equals("Air")) 
        {
          //add that particle to the list of spaces to move into
          openSpaces.add(downRight);
        }
      }
    }

    //if no spaces have been found
    if (openSpaces.size() == 0)
    {
      //check the left/right neighbors for air

      //get left/right neighbor particles
      Particle left = adjacentLeft();
      Particle right = adjacentRight();

      //check the left neighbor for air
      if (left != null && left.type.equals("Air")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(left);
      }

      //check the right neighbor for air
      if (right != null && right.type.equals("Air")) 
      {
        //add to the list of spaces to move into
        openSpaces.add(right);
      }
    }

    //if any potential particles have been found to swap with 
    if (openSpaces.size() != 0) 
    {
      //choose a random available particle
      Particle rand = openSpaces.get(int(random(0, openSpaces.size())));

      //swap this particle with the particle in the chosen space
      grid.swapParticles(this, rand);

      //mark that this particle moved
      moved = true;
    }
  }

  public void interact()
  {
    //get half adjacents
    ArrayList<Particle> halfAdjacents = halfAdjacents();

    //check half adjacents
    for (Particle adj : halfAdjacents) 
    {
      if (adj.type.equals("Lava")) 
      {
        //place a new stone particle where the lava was
        grid.replaceParticle(adj, particleFactory.generateParticle("Stone", "Lava", adj.getIndices()));
        
        //place a new stone particle where this water was
        grid.replaceParticle(this, particleFactory.generateParticle("Stone", "Water", this.getIndices()));

        //marks this particle as interacted
        interacted = true;
      }
    }
  }
}
