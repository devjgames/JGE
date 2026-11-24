package org.game;

public class GameSystem2 extends GameSystem {

    public static interface Post {
        public Texture process(Texture texture) throws Exception;
    }

    private Post post = null;

    public GameSystem2(String sceneName, boolean inDesign, boolean uiEnabled, GameComponent ... factories) throws Exception {
        super("scene1.scene", true, true, factories);
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    protected BasicRenderer createRenderer() throws Exception {
        return new BasicRenderer2();
    }

    @Override
    protected Texture postProcess(Texture texture) throws Exception {
        if(post != null) {
            return post.process(texture);
        }
        return texture;
    }
}