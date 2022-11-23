public class AirParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(0, 0, 0, 0);

  public AirParticle(Point indices, String prevType)
  {
    super(indices, "Air", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    
  }
}
