
//factory that generates particles based on passed in type
public class ParticleFactory
{
    public ParticleFactory()
    {

    }

    public Particle generateParticle(String type, String prevType, Point indices)
    {
        //generate an air particle
        if (type.equals("Air"))
        {
            return new AirParticle(indices, prevType);
        }
        //generate an acid particle
        if (type.equals("Acid"))
        {
            return new AcidParticle(indices, prevType);
        }
        //generate a charcoal particle
        else if (type.equals("Charcoal"))
        {
            return new CharcoalParticle(indices, prevType);
        }
        //generate a fire particle
        else if (type.equals("Fire"))
        {
            return new FireParticle(indices, prevType);
        }
        //generate a ice particle
        else if (type.equals("Ice"))
        {
            return new IceParticle(indices, prevType);
        }
        //generate a lava particle
        else if (type.equals("Lava"))
        {
            return new LavaParticle(indices, prevType);
        }
        //generate a plant particle
        else if (type.equals("Plant"))
        {
            return new PlantParticle(indices, prevType);
        }
        //generate a stone particle
        else if (type.equals("Stone"))
        {
            return new StoneParticle(indices, prevType);
        }
        //generate a water particle
        else if (type.equals("Water"))
        {
            return new WaterParticle(indices, prevType);
        }

        //invalid type, returns null
        else
        {
            println("tried to create particle with unknown type: " + type);
            return null;
        }
    }

    //gets the color of a particle by type
    public Color getColour(String type)
    {
        return generateParticle(type, type, new Point(0,0)).colour;
    }
}