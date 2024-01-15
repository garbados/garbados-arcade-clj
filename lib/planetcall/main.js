import Phaser from 'phaser'
import { WIDTH, HEIGHT } from '../const'
import { IntroScene, TitleScene } from './intro'

class MainScene extends Phaser.Scene {
  create () {
    this.intro = this.scene.add('intro', IntroScene, true, {
      onfinish: () => {
        this.scene.stop('intro')
        this.scene.start('title')
      }
    })
    this.title = this.scene.add('title', TitleScene, false, {
      onnewgame: () => {
        console.log('Hello!')
      }
    })
  }
}

function startGUI (config) {
  const myGame = new Phaser.Game(config) // eslint-disable-line no-unused-vars
  // LMAO I LOVE OBJECT-ORIENTED PROGRAMMING
}

startGUI({
  type: Phaser.AUTO,
  scene: MainScene,
  scale: {
    mode: Phaser.Scale.FIT,
    parent: 'app',
    width: WIDTH,
    height: HEIGHT
  }
})
