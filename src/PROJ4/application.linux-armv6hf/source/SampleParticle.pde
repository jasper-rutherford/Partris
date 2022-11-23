public class SampleParticle extends Particle
{
  //this particle's color
  private Color uniqueColor = new Color(0, 0, 0, 50);

  public SampleParticle(Point indices, String prevType)
  {
    super(indices, "Sample", prevType);
    this.colour = uniqueColor;
  }

  public void move()
  {

  }

  public void interact()
  {
    
  }
}
