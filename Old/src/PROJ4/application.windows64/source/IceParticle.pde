public class IceParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(150, 183, 235);

  public IceParticle(Point indices, String prevType)
  {
    super(indices, "Ice", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    //get half adjacents
    ArrayList<Particle> halfAdjacents = halfAdjacents();

    //check half adjacents 
    for (Particle adj : halfAdjacents) 
    {
      //interact ice with water
      if (adj.type.equals("Water")) 
      {
        //replace that water with a new ice particle
        grid.replaceParticle(adj, "Ice");

        //mark that this particle interacted
        interacted = true;
      }
    }
  }
}
