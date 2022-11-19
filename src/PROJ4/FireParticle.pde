public class FireParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(235, 64, 52);

  private int baseFuel = 500;
  private int fuel;

  public FireParticle(Point indices, String prevType)
  {
    super(indices, "Fire", prevType);
    this.colour = uniqueColor;

    //charcoal starts with more fuel (burns longer)
    if (prevType.equals("Charcoal"))
    {
        fuel = baseFuel * 3;
    }
    //everything else [aka, plants] starts with the standard amount of fuel
    else
    {
      fuel = baseFuel;
    }
  }

  public void move()
  {
    //spend some fuel
    fuel -= int(random(0, baseFuel / 2));

    //if no more fuel
    if (fuel <= 0) 
    {
      //replace this particle with a new air particle
      grid.replaceParticle(this, "Air");
    }

    //mark that this particle moved
    moved = true;
  }

  public void interact()
  {
    //get half adjacents
    ArrayList<Particle> halfAdjacents = halfAdjacents();

    //track if this fire has access to air (assume not)
    boolean hasAir = false;

    //loop through half adjacents 
    for (Particle adj : halfAdjacents) 
    {
      //if the adjacent particle is air
      if (adj.type.equals("Air")) 
      {
        //mark that this fire has access to air 
        hasAir = true;

        //exit loop
        break;
      }
    }

    //if this fire has no air access
    if (!hasAir) 
    {
      println("extinguishing - " + prevType);
      //replace this particle with a particle of prevType
      grid.replaceParticle(this, prevType);

      //mark that this particle interacted
      interacted = true;
    }
    //if the fire has access to air 
    else 
    {
      //loop through the fire's half adjacents      
      for (Particle adj : halfAdjacents) 
      {
        //interact fire with plant
        if (adj.type.equals("Plant")) 
        {
          //get the plant's adjacents
          ArrayList<Particle> plantAdjacents = adj.halfAdjacents();

          //track whether the plant has access to air
          boolean airAccess = false;

          //loop through the plant's half adjacents
          for (Particle plantAdj : plantAdjacents) 
          {
            //if the plant has any air fully adjacent
            if (plantAdj.type.equals("Air")) 
            {
              //mark that the plant has access to air
              airAccess = true;

              //exit loop
              break;
            }
          }

          //if the plant has access to air
          if (airAccess)
          {
            //replace the plant with a new fire particle
            grid.replaceParticle(adj, "Fire");

            //mark that this fire interacted with something
            interacted = true;
          }
        }
        //interact fire with char
        if (adj.type.equals("Charcoal")) 
        {
          //get the char's adjacents
          ArrayList<Particle> charAdjacents = adj.halfAdjacents();

          //track whether the char has access to air
          boolean airAccess = false;

          //loop through the char's half adjacents
          for (Particle charAdj : charAdjacents) 
          {
            //if the char has any air fully adjacent
            if (charAdj.type.equals("Air")) 
            {
              //mark that the char has access to air
              airAccess = true;

              //exit loop
              break;
            }
          }

          //if the char has access to air
          if (airAccess)
          {
            //replace the char with a new fire particle
            grid.replaceParticle(adj, "Fire");

            //mark that this fire interacted with something
            interacted = true;
          }
        }
        //interact fire with ice
        else if (adj.type.equals("Ice")) 
        {
          //replace ice particle with new water particle
          grid.replaceParticle(adj, "Water");

          //mark that this fire interacted with something
          interacted = true;
        }
      }
    }
  }
}
