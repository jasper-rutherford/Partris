import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Random;

public class LavaParticle extends Particle
{
    //this particle's color
    private Color uniqueColor = new Color(214, 111, 32);

    public LavaParticle(Point indices, String prevType)
    {
        super(indices, "Lava", prevType);
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
            if (down.type.equals("Air") || down.type.equals("Acid"))
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
                if (downLeft != null && (downLeft.type.equals("Air") || downLeft.type.equals("Acid")))
                {
                    //add that particle to the list of spaces to move into
                    openSpaces.add(downLeft);
                }

                //check the right diagonal for air
                if (downRight != null && (downRight.type.equals("Air") || downRight.type.equals("Acid")))
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
            if (left != null && (left.type.equals("Air") || left.type.equals("Acid")))
            {
                //add to the list of spaces to move into
                openSpaces.add(left);
            }

            //check the right neighbor for air
            if (right != null && (right.type.equals("Air") || right.type.equals("Acid")))
            {
                //add to the list of spaces to move into
                openSpaces.add(right);
            }
        }

        //if any potential particles have been found to swap with
        if (openSpaces.size() != 0)
        {
            //choose a random available particle
            Particle rand = openSpaces.get((new Random()).nextInt(openSpaces.size()));

            //swap this particle with the particle in the chosen space
            Grid.getGrid().swapParticles(this, rand);

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
            //interact lava with water
            if (adj.type.equals("Water"))
            {
                //place a new stone particle where the water was
                Grid.getGrid().replaceParticle(adj, "Stone");

                //place a new stone particle where this lava was
                Grid.getGrid().replaceParticle(this, "Stone");

                //marks this particle as interacted
                interacted = true;
            }
            //interact lava with plant
            else if (adj.type.equals("Plant"))
            {
                //check plant's half adjacents for air

                //get plant's half adjacents
                ArrayList<Particle> plantAdjacents = adj.halfAdjacents();

                //track whether the plant has access to air
                boolean hasAir = false;

                //loop through all plant half adjacents
                for (Particle plantAdj : plantAdjacents)
                {
                    //if the plant's neighbor is air
                    if (plantAdj.type.equals("Air"))
                    {
                        //mark that the plant has access to air
                        hasAir = true;

                        //stop looping - air has been found
                        break;
                    }
                }

                //if the plant has air fully adjacent
                if (hasAir)
                {
                    //replace the plant with a fire particle
                    Grid.getGrid().replaceParticle(adj, "Fire");
                }
                //if the plant has no air
                else
                {
                    //replace the plant with a charcoal particle
                    Grid.getGrid().replaceParticle(adj, "Charcoal");
                }

                //mark that this particle interacted with something
                interacted = true;
            }
            //interact lava with charcoal
            else if (adj.type.equals("Charcoal"))
            {
                //get charcoal's half adjacents
                ArrayList<Particle> charAdjacents = adj.halfAdjacents();

                //track whether the charcoal has access to air
                boolean hasAir = false;

                //loop through all plant half adjacents
                for (Particle charAdj : charAdjacents)
                {
                    //if the charcoal's neighbor is air
                    if (charAdj.type.equals("Air"))
                    {
                        //mark that the charcoal has access to air
                        hasAir = true;

                        //stop looping - air has been found
                        break;
                    }
                }

                //if the char has air fully adjacent
                if (hasAir)
                {
                    //replace the plant with a fire particle
                    Grid.getGrid().replaceParticle(adj, "Fire");
                }

                //mark that this particle interacted with something
                interacted = true;
            }
            //interact lava with ice
            else if (adj.type.equals("Ice"))
            {
                //replace the ice with a water particle
                Grid.getGrid().replaceParticle(adj, "Water");

                //mark that this particle interacted with something
                interacted = true;
            }
            // //interact lava with stone
            // else if (adj.type.equals("Stone"))
            // {
            //   //replace the stone with a lava particle
            //   Grid.getGrid().replaceParticle(adj, "Lava");

            //   //mark that this particle interacted with something
            //   interacted = true;
            // }
        }
    }
}
