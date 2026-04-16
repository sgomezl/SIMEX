package com.mygdx.primelogistics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BoatCatchGame extends ApplicationAdapter {

    // 比例和游戏参数
    private static final float WATER_HEIGHT_RATIO = 0.20f;
    private static final float BOAT_WIDTH_RATIO = 220f / 720f;
    private static final float PACKAGE_SIZE_RATIO = 88f / 720f;
    private static final float BOAT_SPEED_RATIO = 560f / 720f;
    private static final float MIN_PACKAGE_SPEED_RATIO = 140f / 1280f;
    private static final float MAX_PACKAGE_SPEED_RATIO = 200f / 1280f;
    private static final float SPAWN_MARGIN_RATIO = 30f / 720f;
    private static final float BOAT_Y_OFFSET_RATIO = 60f / 1280f;
    private static final float WATER_BOTTOM_OVERFLOW_RATIO = 80f / 1280f;
    private static final float HUD_PADDING_RATIO = 24f / 720f;
    private static final float HUD_LINE_GAP_RATIO = 48f / 1280f;
    private static final float PANEL_WIDTH_RATIO = 0.76f;
    private static final float PANEL_HEIGHT_RATIO = 0.34f;
    private static final float BUTTON_WIDTH_RATIO = 0.42f;
    private static final float BUTTON_HEIGHT_RATIO = 0.075f;
    private static final float HUD_PANEL_HEIGHT_RATIO = 0.14f;
    private static final float GAME_DURATION_SECONDS = 30f;
    private static final float SPAWN_INTERVAL = 1.15f;
    private static final float WATER_U_SPAN = 1.45f;
    private static final float MAX_DELTA = 1f / 30f;

    // 软件配色
    private static final Color APP_BACKGROUND = new Color(0.976f, 0.980f, 0.984f, 1f);
    private static final Color APP_PRIMARY = new Color(0.078f, 0.337f, 0.600f, 1f);
    private static final Color APP_SECONDARY = new Color(0.125f, 0.671f, 0.941f, 1f);
    private static final Color APP_ACCENT = new Color(0.992f, 0.502f, 0.212f, 1f);
    private static final Color APP_TEXT = new Color(0.035f, 0.047f, 0.031f, 1f);

    private final GameNavigationHandler navigationHandler;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture boatTexture;
    private Texture packageTexture;
    private Texture waterTexture;

    private BitmapFont hudFont;
    private BitmapFont infoFont;
    private BitmapFont titleFont;
    private BitmapFont scoreFont;
    private BitmapFont buttonFont;
    private GlyphLayout glyphLayout;

    private Rectangle boatBounds;
    private Rectangle returnButtonBounds;
    private Array<FallingPackage> fallingPackages;
    private Vector3 touchPoint;

    private float worldWidth;
    private float worldHeight;

    private float waterHeight;
    private float boatWidth;
    private float boatHeight;
    private float packageSize;
    private float boatSpeed;
    private float minPackageSpeed;
    private float maxPackageSpeed;
    private float spawnMargin;
    private float boatYOffset;
    private float waterBottomOverflow;
    private float hudPadding;
    private float hudLineGap;
    private float panelWidth;
    private float panelHeight;
    private float buttonWidth;
    private float buttonHeight;
    private float hudPanelX;
    private float hudPanelY;
    private float hudPanelWidth;
    private float hudPanelHeight;
    private float hudCornerRadius;
    private float panelCornerRadius;
    private float buttonCornerRadius;

    private float spawnTimer;
    private float waterScroll;
    private float waterCurrent;
    private float timeLeft;

    private boolean gameOver;
    private int score;

    public BoatCatchGame() {
        this(null);
    }

    public BoatCatchGame(GameNavigationHandler navigationHandler) {
        this.navigationHandler = navigationHandler;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        boatTexture = new Texture("images/boat.png");
        packageTexture = new Texture("images/package.png");
        waterTexture = new Texture("images/water1.png");
        waterTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

        glyphLayout = new GlyphLayout();
        boatBounds = new Rectangle();
        returnButtonBounds = new Rectangle();
        fallingPackages = new Array<>();
        touchPoint = new Vector3();

        timeLeft = GAME_DURATION_SECONDS;
        score = 0;
        gameOver = false;
        spawnTimer = 0f;
        waterScroll = 0f;
        waterCurrent = 0f;

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        for (int i = 0; i < 3; i++) {
            spawnPackage(i * 0.18f * worldHeight);
        }
    }

    @Override
    public void render() {
        float delta;

        delta = Math.min(Gdx.graphics.getDeltaTime(), MAX_DELTA);

        if (gameOver) {
            handleGameOverInput();
        } else {
            updateGame(delta);
        }

        drawGame();
    }

    private void updateGame(float delta) {
        FallingPackage currentPackage;
        float boatShift;

        timeLeft = Math.max(0f, timeLeft - delta);
        if (timeLeft <= 0f) {
            endGame();
            return;
        }

        boatShift = moveBoat(delta);
        updateWater(boatShift, delta);

        spawnTimer += delta;
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnPackage(0f);
        }

        for (int i = fallingPackages.size - 1; i >= 0; i--) {
            currentPackage = fallingPackages.get(i);
            currentPackage.bounds.y -= currentPackage.speed * delta;

            if (currentPackage.bounds.overlaps(boatBounds)) {
                score++;
                fallingPackages.removeIndex(i);
            } else if (currentPackage.bounds.y + currentPackage.bounds.height <= waterHeight) {
                fallingPackages.removeIndex(i);
            }
        }
    }

    private void endGame() {
        gameOver = true;
        fallingPackages.clear();
    }

    private void handleGameOverInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touchPoint);

        if (returnButtonBounds.contains(touchPoint.x, touchPoint.y)) {
            if (navigationHandler != null) {
                navigationHandler.returnToLogin();
            } else {
                Gdx.app.exit();
            }
        }
    }

    private float moveBoat(float delta) {
        float previousX;
        float movement;

        previousX = boatBounds.x;
        movement = getTiltMovement() * boatSpeed * delta;

        boatBounds.x += movement;

        if (Gdx.input.isTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.unproject(touchPoint);
            boatBounds.x = touchPoint.x - boatBounds.width / 2f;
        }

        boatBounds.x = MathUtils.clamp(boatBounds.x, 0f, worldWidth - boatBounds.width);
        return boatBounds.x - previousX;
    }

    private float getTiltMovement() {
        float horizontalTilt;

        if (!Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer)) {
            return 0f;
        }

        horizontalTilt = -Gdx.input.getAccelerometerX() / 4.5f;
        return MathUtils.clamp(horizontalTilt, -1f, 1f);
    }

    private void updateWater(float boatShift, float delta) {
        if (worldWidth <= 0f) {
            return;
        }

        waterCurrent += (boatShift / worldWidth) * 12f;
        waterCurrent *= MathUtils.clamp(1f - delta * 3f, 0f, 1f);
        waterScroll += waterCurrent * delta;
    }

    private void spawnPackage(float extraOffsetY) {
        float spawnX;
        float spawnY;
        float speed;

        spawnX = MathUtils.random(spawnMargin, worldWidth - packageSize - spawnMargin);
        spawnY = worldHeight + MathUtils.random(worldHeight * 0.02f, worldHeight * 0.11f) + extraOffsetY;
        speed = MathUtils.random(minPackageSpeed, maxPackageSpeed);

        fallingPackages.add(new FallingPackage(spawnX, spawnY, packageSize, speed));
    }

    private void drawGame() {
        ScreenUtils.clear(APP_BACKGROUND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawWater();
        drawPackages();
        drawBoat();
        batch.end();

        drawHudChrome();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawHud();
        batch.end();

        if (gameOver) {
            drawGameOverOverlay();
        }
    }

    private void drawWater() {
        batch.draw(
            waterTexture,
            0f,
            -waterBottomOverflow,
            worldWidth,
            waterHeight + waterBottomOverflow,
            waterScroll,
            1f,
            waterScroll + WATER_U_SPAN,
            0f
        );
    }

    private void drawPackages() {
        for (FallingPackage currentPackage : fallingPackages) {
            batch.draw(
                packageTexture,
                currentPackage.bounds.x,
                currentPackage.bounds.y,
                currentPackage.bounds.width,
                currentPackage.bounds.height
            );
        }
    }

    private void drawBoat() {
        batch.draw(boatTexture, boatBounds.x, boatBounds.y, boatBounds.width, boatBounds.height);
    }

    private void drawHud() {
        String scoreText;
        String timeText;
        float scoreY;
        float timeY;
        float hintY;

        scoreText = "Puntuacion: " + score;
        timeText = "Tiempo: " + MathUtils.ceil(timeLeft) + "s";
        scoreY = hudPanelY + hudPanelHeight - (hudPadding * 0.95f);
        timeY = scoreY;
        hintY = hudPanelY + (hudPadding * 1.55f);

        hudFont.setColor(APP_BACKGROUND);
        hudFont.draw(batch, scoreText, hudPanelX + (hudPadding * 0.75f), scoreY);

        glyphLayout.setText(hudFont, timeText);
        hudFont.draw(batch, glyphLayout, hudPanelX + hudPanelWidth - glyphLayout.width - (hudPadding * 0.75f), timeY);

        infoFont.setColor(APP_BACKGROUND);
        infoFont.draw(batch, "Inclina el movil para mover la barca", hudPanelX + (hudPadding * 0.75f), hintY);
    }

    private void drawHudChrome() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawRoundedRect(
            hudPanelX + (hudPadding * 0.16f),
            hudPanelY - (hudPadding * 0.16f),
            hudPanelWidth,
            hudPanelHeight,
            hudCornerRadius,
            new Color(APP_SECONDARY.r, APP_SECONDARY.g, APP_SECONDARY.b, 0.34f)
        );

        drawRoundedRect(hudPanelX, hudPanelY, hudPanelWidth, hudPanelHeight, hudCornerRadius, APP_PRIMARY);

        shapeRenderer.end();
    }

    private void drawGameOverOverlay() {
        float panelX;
        float panelY;
        float headerX;
        float headerY;
        float headerWidth;
        float headerHeight;

        panelX = (worldWidth - panelWidth) / 2f;
        panelY = (worldHeight - panelHeight) / 2f;
        headerX = panelX + (panelWidth * 0.08f);
        headerY = panelY + (panelHeight * 0.73f);
        headerWidth = panelWidth * 0.84f;
        headerHeight = panelHeight * 0.16f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0f, 0f, 0f, 0.42f);
        shapeRenderer.rect(0f, 0f, worldWidth, worldHeight);

        drawRoundedRect(
            panelX + (hudPadding * 0.18f),
            panelY - (hudPadding * 0.18f),
            panelWidth,
            panelHeight,
            panelCornerRadius,
            new Color(APP_PRIMARY.r, APP_PRIMARY.g, APP_PRIMARY.b, 0.18f)
        );

        drawRoundedRect(panelX, panelY, panelWidth, panelHeight, panelCornerRadius, APP_BACKGROUND);
        drawRoundedRect(headerX, headerY, headerWidth, headerHeight, panelCornerRadius * 0.7f, APP_PRIMARY);

        drawRoundedRect(
            returnButtonBounds.x + (hudPadding * 0.12f),
            returnButtonBounds.y - (hudPadding * 0.12f),
            returnButtonBounds.width,
            returnButtonBounds.height,
            buttonCornerRadius,
            new Color(APP_PRIMARY.r, APP_PRIMARY.g, APP_PRIMARY.b, 0.22f)
        );

        drawRoundedRect(
            returnButtonBounds.x,
            returnButtonBounds.y,
            returnButtonBounds.width,
            returnButtonBounds.height,
            buttonCornerRadius,
            APP_ACCENT
        );

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawCenteredText(titleFont, "Tiempo Terminado", headerY + (headerHeight * 0.73f), APP_BACKGROUND);
        drawCenteredText(infoFont, "Resultado final", panelY + (panelHeight * 0.58f), APP_PRIMARY);
        drawCenteredText(scoreFont, String.valueOf(score), panelY + (panelHeight * 0.50f), APP_TEXT);
        drawCenteredText(buttonFont, "Volver al login", returnButtonBounds.y + (returnButtonBounds.height * 0.64f), APP_BACKGROUND);
        batch.end();
    }

    private void drawCenteredText(BitmapFont currentFont, String text, float y, Color color) {
        Color oldColor;

        oldColor = new Color(currentFont.getColor());
        currentFont.setColor(color);
        glyphLayout.setText(currentFont, text);
        currentFont.draw(batch, glyphLayout, (worldWidth - glyphLayout.width) / 2f, y);
        currentFont.setColor(oldColor);
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        float clampedRadius;

        clampedRadius = Math.min(radius, Math.min(width, height) / 2f);

        shapeRenderer.setColor(color);
        shapeRenderer.rect(x + clampedRadius, y, width - (clampedRadius * 2f), height);
        shapeRenderer.rect(x, y + clampedRadius, clampedRadius, height - (clampedRadius * 2f));
        shapeRenderer.rect(x + width - clampedRadius, y + clampedRadius, clampedRadius, height - (clampedRadius * 2f));
        shapeRenderer.circle(x + clampedRadius, y + clampedRadius, clampedRadius);
        shapeRenderer.circle(x + width - clampedRadius, y + clampedRadius, clampedRadius);
        shapeRenderer.circle(x + clampedRadius, y + height - clampedRadius, clampedRadius);
        shapeRenderer.circle(x + width - clampedRadius, y + height - clampedRadius, clampedRadius);
    }

    @Override
    public void resize(int width, int height) {
        float previousWorldWidth;
        float previousWorldHeight;
        float previousBoatCenterX;
        float panelX;
        float panelY;
        float buttonY;

        previousWorldWidth = worldWidth;
        previousWorldHeight = worldHeight;
        previousBoatCenterX = boatBounds.x + (boatBounds.width / 2f);

        viewport.update(width, height, true);
        worldWidth = viewport.getWorldWidth();
        worldHeight = viewport.getWorldHeight();

        camera.position.set(worldWidth / 2f, worldHeight / 2f, 0f);
        camera.update();

        if (boatTexture == null) {
            return;
        }

        waterHeight = worldHeight * WATER_HEIGHT_RATIO;
        boatWidth = worldWidth * BOAT_WIDTH_RATIO;
        boatHeight = boatWidth * (boatTexture.getHeight() / (float) boatTexture.getWidth());
        packageSize = worldWidth * PACKAGE_SIZE_RATIO;
        boatSpeed = worldWidth * BOAT_SPEED_RATIO;
        minPackageSpeed = worldHeight * MIN_PACKAGE_SPEED_RATIO;
        maxPackageSpeed = worldHeight * MAX_PACKAGE_SPEED_RATIO;
        spawnMargin = worldWidth * SPAWN_MARGIN_RATIO;
        boatYOffset = worldHeight * BOAT_Y_OFFSET_RATIO;
        waterBottomOverflow = worldHeight * WATER_BOTTOM_OVERFLOW_RATIO;
        hudPadding = worldWidth * HUD_PADDING_RATIO;
        hudLineGap = worldHeight * HUD_LINE_GAP_RATIO;
        panelWidth = worldWidth * PANEL_WIDTH_RATIO;
        panelHeight = worldHeight * PANEL_HEIGHT_RATIO;
        buttonWidth = worldWidth * BUTTON_WIDTH_RATIO;
        buttonHeight = worldHeight * BUTTON_HEIGHT_RATIO;
        hudPanelWidth = worldWidth - hudPadding;
        hudPanelHeight = worldHeight * HUD_PANEL_HEIGHT_RATIO;
        hudPanelX = hudPadding * 0.5f;
        hudPanelY = worldHeight - hudPanelHeight - (hudPadding * 0.55f);
        hudCornerRadius = hudPanelHeight * 0.22f;
        panelCornerRadius = Math.min(panelWidth, panelHeight) * 0.08f;
        buttonCornerRadius = buttonHeight * 0.28f;

        rebuildFonts();

        boatBounds.setSize(boatWidth, boatHeight);
        boatBounds.y = waterHeight - boatYOffset;

        if (previousWorldWidth == 0f) {
            boatBounds.x = (worldWidth - boatBounds.width) / 2f;
        } else {
            boatBounds.x = ((previousBoatCenterX / previousWorldWidth) * worldWidth) - (boatBounds.width / 2f);
        }

        boatBounds.x = MathUtils.clamp(boatBounds.x, 0f, worldWidth - boatBounds.width);

        panelX = (worldWidth - panelWidth) / 2f;
        panelY = (worldHeight - panelHeight) / 2f;
        buttonY = panelY + (panelHeight * 0.06f);

        returnButtonBounds.set(
            panelX + (panelWidth - buttonWidth) / 2f,
            buttonY,
            buttonWidth,
            buttonHeight
        );

        if (previousWorldWidth > 0f && previousWorldHeight > 0f) {
            for (FallingPackage currentPackage : fallingPackages) {
                currentPackage.bounds.x = (currentPackage.bounds.x / previousWorldWidth) * worldWidth;
                currentPackage.bounds.y = (currentPackage.bounds.y / previousWorldHeight) * worldHeight;
                currentPackage.bounds.width = packageSize;
                currentPackage.bounds.height = packageSize;
                currentPackage.speed = (currentPackage.speed / previousWorldHeight) * worldHeight;
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        boatTexture.dispose();
        packageTexture.dispose();
        waterTexture.dispose();
        disposeFonts();
    }

    private void rebuildFonts() {
        disposeFonts();
        hudFont = createFont("fonts/montserrat_bold.ttf", Math.max(36, Math.round(worldWidth * 0.044f)));
        infoFont = createFont("fonts/montserrat_medium.ttf", Math.max(22, Math.round(worldWidth * 0.029f)));
        titleFont = createFont("fonts/montserrat_bold.ttf", Math.max(40, Math.round(worldWidth * 0.050f)));
        scoreFont = createFont("fonts/montserrat_bold.ttf", Math.max(58, Math.round(worldWidth * 0.080f)));
        buttonFont = createFont("fonts/montserrat_bold.ttf", Math.max(26, Math.round(worldWidth * 0.034f)));
    }

    private void disposeFonts() {
        if (hudFont != null) {
            hudFont.dispose();
            hudFont = null;
        }
        if (infoFont != null) {
            infoFont.dispose();
            infoFont = null;
        }
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (scoreFont != null) {
            scoreFont.dispose();
            scoreFont = null;
        }
        if (buttonFont != null) {
            buttonFont.dispose();
            buttonFont = null;
        }
    }

    private BitmapFont createFont(String path, int size) {
        FreeTypeFontGenerator generator;
        FreeTypeFontGenerator.FreeTypeFontParameter parameter;
        BitmapFont generatedFont;

        generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.minFilter = TextureFilter.Linear;
        parameter.magFilter = TextureFilter.Linear;

        generatedFont = generator.generateFont(parameter);
        generator.dispose();
        return generatedFont;
    }

    private static class FallingPackage {
        private final Rectangle bounds;
        private float speed;

        private FallingPackage(float x, float y, float size, float speed) {
            bounds = new Rectangle(x, y, size, size);
            this.speed = speed;
        }
    }
}
