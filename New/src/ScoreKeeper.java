import processing.core.PApplet;

public class ScoreKeeper
{
    //singleton
    private static ScoreKeeper scoreKeeper;

    private int score;

    //amount of points gained per plant burned
    private final int plantBurnPoints = 2;

    //amount of points gained per charcoal burned
    private final int charcoalBurnPoints = 3;

    //amount of points gained per particle dissolved by acid
    private final int dissolvePoints = 3;

    //amount of points gained per particle cleared via row clear
    private final int pointsPerParticleCleared = 5;

    private int levelPoints;
    private final int levelPointsPerLevel = Grid.getGrid().calcLevelPoints(pointsPerParticleCleared);


    private ScoreKeeper()
    {
        score = 0;
        levelPoints = 0;
    }

    //singleton
    public static ScoreKeeper getScoreKeeper()
    {
        if (scoreKeeper == null)
        {
            scoreKeeper = new ScoreKeeper();
        }

        return scoreKeeper;
    }

    public void resetScoreKeeper()
    {
        scoreKeeper = new ScoreKeeper();
    }

    public void burnPlants(int numPlants)
    {
        addPoints(numPlants * plantBurnPoints);
    }

    public void burnCharcoal(int numCharcoal)
    {
        addPoints(numCharcoal * charcoalBurnPoints);
    }

    public void dissolveParticles(int numParticles)
    {
        addPoints(numParticles * dissolvePoints);
    }

    public void clearParticles(int numParticles)
    {
        addPoints(numParticles * pointsPerParticleCleared);
    }

    public void addPoints(int points)
    {
        //increase score [points multiplier based on how fast the tetromino is falling]
        score += (int)(points * (Grid.getGrid().calcScoreLevelModifier()));

        //add points to level tracker
        levelPoints += points;

        //go up a level
        if (levelPoints >= levelPointsPerLevel)
        {
            levelPoints -= levelPointsPerLevel;

            Grid.getGrid().levelUp();

            System.out.println("Level Points: " + levelPoints + "/" + levelPointsPerLevel);
            System.out.println("~~~~~~~~~~~");
        }
    }

    public void render(Main drawer)
    {
        int blockWidth = Grid.getGrid().blockWidth;
        float cornerX = Grid.getGrid().cornerX;
        float cornerY = Grid.getGrid().cornerY;


        //render the score
        drawer.drawTextWithBorder("Score: " + score, cornerX - blockWidth * 7, cornerY - blockWidth / 4, 25, new Color(255, 255, 255), new Color(0, 0, 0));

        //render the level counter
        drawer.drawTextWithBorder("Level: " + Grid.getGrid().getLevel(), cornerX + blockWidth * 11, cornerY - blockWidth * 1.25f, 25, new Color(255, 255, 255), new Color(0, 0, 0));

        //render level points bar
        drawer.fill(new Color(199, 199, 199));
        drawer.rect(cornerX + 11.5f * blockWidth, cornerY - blockWidth * 5 / 6, 5 * blockWidth, 2.0f / 3 * blockWidth);
        drawer.fill(new Color(61, 132, 245));
        drawer.rect(cornerX + 11.5f * blockWidth, cornerY - blockWidth * 5 / 6, 5 * blockWidth * levelPoints / levelPointsPerLevel, 2.0f / 3 * blockWidth);

        //render the level points
        drawer.drawTextWithBorder(levelPoints + "/" + levelPointsPerLevel, cornerX + blockWidth * 12, cornerY - blockWidth / 3.7f, 15, new Color(255, 255, 255), new Color(0, 0, 0));
        drawer.drawTextWithBorder("(" + PApplet.parseInt(levelPoints * 100 / levelPointsPerLevel) + "%)", cornerX + blockWidth * (12 + 4.75f), cornerY - blockWidth / 3.7f, 15, new Color(255, 255, 255), new Color(0, 0, 0));

    }
}
