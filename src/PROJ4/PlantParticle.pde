public class PlantParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(50, 168, 82);

  public PlantParticle(Point indices, String prevType)
  {
    super(indices, "Plant", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    //get full adjacents
    ArrayList<Particle> fullAdjacents = fullAdjacents();

    //check all directly adjacent particles
    for (Particle adj : fullAdjacents) 
    {
      //interact plant with air
      if (adj.type.equals("Air")) 
      {
        //if the adjacent particle is air, plant could grow there. 

        //get all the particles adjacent to the air particles
        ArrayList<Particle> airAdjacents = adj.halfAdjacents();

        //track whether the air is growable
        boolean growable = false;

        //loop through the air's neighbors
        for (Particle airAdj : airAdjacents) 
        {
          //if the air's neighbor isn't air or plant
          if (!airAdj.type.equals("Air") && !airAdj.type.equals("Plant")) 
          {
            //then it is a viable spot to grow, mark it as such
            growable = true;

            //exit loop
            break;
          }
        }   

        //if the air is growable
        if (growable) 
        {
          //place a new plant particle where the air was
          grid.replaceParticle(adj, particleFactory.generateParticle("Plant", "Air", adj.getIndices()));

          //marks this particle as interacted
          interacted = true;
        }
      }
    }
  }
}
