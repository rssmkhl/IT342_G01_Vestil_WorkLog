import axios from 'axios';

(async () => {
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      usernameOrEmail: 'user',
      password: 'User@12345'
    }, { headers: { 'Content-Type': 'application/json' } });
    console.log('status', res.status);
    console.log(res.data);
  } catch (e) {
    if (e.response) {
      console.error('status', e.response.status);
      console.error('data', e.response.data);
    } else {
      console.error(e.message);
    }
    process.exit(1);
  }
})();
