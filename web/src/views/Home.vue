<template>
  <section>
    <nav class="ui fixed inverted menu">
      <div class="ui container">
        <a href="/" class="header item">Material Explorer</a>
        <a href="#" class="item">すべての素材</a>
        <div class="right menu">
          <div class="item">
            <div class="ui icon input">
              <input type="text" placeholder="検索">
              <i class="search link icon"></i>
            </div>
          </div>
        </div>
      </div>
    </nav>
    <div class="ui main container">
      <template v-if="thumbs !== null && repositories !== null">
        <h3>画像</h3>
        <section id="thumbnails" class="ui five column grid">
          <div class="column" v-for="(item, i) in thumbs" :key="i">
            <div class="ui fluid card">
              <div class="image">
                <img :src="getThumbnail(item)" :width="item.thumbnail.width" :height="item.thumbnail.height">
                <div class="content">
                  <a class="header">{{ item.file.name }}</a>
                  <div class="meta">
                    <span class="date">{{ item.file.path }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <h3>プロジェクト一覧</h3>
        <table id="projects" class="ui single line table">
          <thead>
            <tr>
              <th>プロジェクト名</th>
              <th>タイトル</th>
              <th>最後の活動</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, i) in repositories" :key="i">
              <td>{{ item.name }}</td>
              <td>{{ item.title }}</td>
              <td></td>
            </tr>
          </tbody>
        </table>
      </template>
      <div id="error-message" class="ui negative message" v-else-if="isError">
        <p>データを読み込めませんでした。</p>
      </div>
      <div id="loader" style="padding-top: 8em" v-else>
        <div class="ui active centered inline loader"></div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import MaterialExplorer from '@/api/MaterialExplorer';
import ThumbnailFile from '@/models/ThumbnailFile';
import Repository from '@/models/Repository';
import RepositoryItem from '@/models/RepositoryItem';
import Dictionaries from '@/util/Dictionaries';

@Component
export default class Home extends Vue {
  public thumbs: ThumbnailFile[] | null = null;
  public repositories: Repository[] | null = null;
  public isError: boolean = false;

  get repositoryDictionary(): { [key: string] : Repository } {
    if (this.repositories !== null) {
      return Dictionaries.toDictionary(this.repositories, e => e.id);
    } else {
      throw new TypeError();
    }
  }

  public getThumbnail(thumb: ThumbnailFile): string {
    return MaterialExplorer.resources.thumbnail(thumb.file.id);
  }

  public mounted(): void {
    const resources = MaterialExplorer.resources;
    Promise.all([resources.images(), resources.repositories()])
      .then(dataSet => {
        this.thumbs = dataSet[0].items;
        this.repositories = dataSet[1].items;
      }).catch(error => {
        this.isError = true;
      });
  }
}
</script>
