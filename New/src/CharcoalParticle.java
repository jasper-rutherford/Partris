public class CharcoalParticle extends Particle
{
    //this particle's color
    private Color uniqueColor = new Color(43, 43, 43);
    public int fuel = -1;

    public CharcoalParticle(Point indices, String prevType)
    {
        super(indices, "Charcoal", prevType);
        this.colour = uniqueColor;
    }

    public void move()
    {

    }

    public void interact()
    {

    }
}
