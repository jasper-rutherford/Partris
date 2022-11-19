
//factory that generates particles based on passed in type
public class ParticleFactory
{
    public ParticleFactory()
    {

    }

    public Particle generateParticle(String type, Particle prevParticle)
    {
        //generate an air particle
        if (type.equals("Air"))
        {
            return new AirParticle(prevParticle.indices, prevParticle.type);
        }
        //generate an acid particle
        else if (type.equals("Acid"))
        {
            return new AcidParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a charcoal particle
        else if (type.equals("Charcoal"))
        {
            CharcoalParticle charcoal = new CharcoalParticle(prevParticle.indices, prevParticle.type);
            
            //conserve fuel
            if (prevParticle instanceof FireParticle)
            {
                FireParticle fire = (FireParticle)prevParticle;
                charcoal.fuel = fire.fuel; 
            }

            return charcoal;
        }
        //generate a fire particle
        else if (type.equals("Fire"))
        {
            FireParticle fire = new FireParticle(prevParticle.indices, prevParticle.type);

            //conserve fuel
            if (prevParticle instanceof PlantParticle && ((PlantParticle)prevParticle).fuel != -1)
            {
                fire.fuel = ((PlantParticle)prevParticle).fuel;
            }
            else if (prevParticle instanceof CharcoalParticle && ((CharcoalParticle)prevParticle).fuel != -1)
            {
                fire.fuel = ((CharcoalParticle)prevParticle).fuel;
            }

            return fire;
        }
        //generate a goop particle
        else if (type.equals("Goop"))
        {
            return new GoopParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a ice particle
        else if (type.equals("Ice"))
        {
            return new IceParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a lava particle
        else if (type.equals("Lava"))
        {
            return new LavaParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a plant particle
        else if (type.equals("Plant"))
        {
            PlantParticle plant = new PlantParticle(prevParticle.indices, prevParticle.type);

            //conserve fuel
            if (prevParticle instanceof FireParticle)
            {
                FireParticle fire = (FireParticle)prevParticle;
                plant.fuel = fire.fuel; 
            }

            return plant;
        }
        //generate a stone particle
        else if (type.equals("Stone"))
        {
            return new StoneParticle(prevParticle.indices, prevParticle.type);
        }
        //generate a water particle
        else if (type.equals("Water"))
        {
            return new WaterParticle(prevParticle.indices, prevParticle.type);
        }

        //invalid type, returns null
        else
        {
            println("tried to create particle with unknown type: " + type);
            return null;
        }
    }

    //generates an air particle with a previous type which is also air
    public Particle generateAirParticle(Point indices)
    {
        return new AirParticle(indices, "Air");
    }

    //gets the color of a particle by type 
    //yes this is dumb
    public Color getColour(String type)
    {
        println("getting color of " + type);
        return generateParticle(type, generateAirParticle(new Point(0,0))).colour;
    }
}