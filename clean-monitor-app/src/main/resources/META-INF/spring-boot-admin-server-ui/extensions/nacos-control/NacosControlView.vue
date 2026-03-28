package META

<template>
  <div class="nacos-control">
    <el-button 
      type="primary" 
      :loading="loading"
      @click="toggleInstance">
      {{ instanceEnabled ? '下线' : '上线' }}
    </el-button>
    <span v-if="result" class="result">{{ result }}</span>
  </div>
</template>

<script>
export default {
  props: {
    instance: Object  // SBA 会传入当前实例对象
  },
  data() {
    return {
      loading: false,
      result: '',
      instanceEnabled: true
    };
  },
  mounted() {
    // 检查实例当前状态（可以从 instance.metadata 中获取权重信息）
    this.instanceEnabled = this.instance.metadata?.enabled !== 'false';
  },
  methods: {
    async toggleInstance() {
      this.loading = true;
      this.result = '';
      
      const action = this.instanceEnabled ? 'offline' : 'online';
      const url = `/api/nacos/${action}?serviceName=${this.instance.serviceName}&ip=${this.instance.ip}&port=${this.instance.port}`;
      
      try {
        const response = await this.instance.axios.post(url);
        if (response.data.success) {
          this.result = `${action === 'offline' ? '下线' : '上线'}成功`;
          this.instanceEnabled = !this.instanceEnabled;
        } else {
          this.result = '操作失败';
        }
      } catch (err) {
        this.result = '操作异常：' + err.message;
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>